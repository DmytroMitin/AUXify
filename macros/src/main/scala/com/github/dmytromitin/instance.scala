package com.github.dmytromitin

import macrocompat.bundle
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class instance extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro InstanceMacro.impl
}

@bundle
class InstanceMacro(val c: whitebox.Context) extends Helpers {
  import c.universe._

  def impl(annottees: Tree*): Tree = {
    def createFunctionType(paramss: Seq[Seq[Tree]], tpt: Tree): Tree =
      paramss.foldRight(tpt: Tree)((params: Seq[Tree], acc: Tree) => tq"(..$params) => $acc")

    def modifyType(tpt: Tree, typeNameMap: Map[TypeName, TypeName]): Tree = {
      val transformer = new Transformer {
        override def transform(tree: Tree): Tree = tree match {
          case tq"${name: TypeName}" => tq"${typeNameMap.applyOrElse(name, identity[TypeName])}"
          case _ => super.transform(tree)
        }
      }

      transformer.transform(tpt)
    }

    def modifyStats(stats: Seq[Tree], typeNameMap: Map[TypeName, TypeName]): (Seq[TypeDef], Seq[TypeDef], Seq[Tree], Seq[Tree]) = {
      val res: Seq[(Option[TypeDef], Option[TypeDef], Option[Tree], Tree)] = stats.map {
        case q"${mods: Modifiers} type $name[..$tparams] >: $low <: $high" =>
          val name0 = typeNameMap(name)
          val modifiedTparams = modifyTparams(tparams)
          val stat = q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} type $name[..${modifiedTparams._1}] = $name0[..${modifiedTparams._2}]"
          (
            Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM} type $name0[..$tparams] >: $low <: $high"),
            Some(stat),
            None,
            stat
          )

        case q"${mods: Modifiers} def $tname[..$tparams](...$paramss): $tpt = ${`EmptyTree`}" =>
          if (tparams.isEmpty) {
            val f = TermName(c.freshName("f"))
            val (paramssTypess, paramssNamess) = modifyParamss(paramss)
            val domain = paramssTypess.map(_.map(modifyType(_, typeNameMap)))
            val codomain = modifyType(tpt, typeNameMap)
            val functionType = createFunctionType(domain, codomain)
            (
              None,
              None,
              Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM} val $f: $functionType = $EmptyTree"),
              q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} def $tname[..${Seq[Tree]()}](...$paramss): $tpt = $f(...$paramssNamess)"
            )
          } else c.abort(c.enclosingPosition, "polymorphic method")

        case q"$mods val $tname: $tpt = ${`EmptyTree`}" =>
          val f = TermName(c.freshName("f"))
          val codomain = modifyType(tpt, typeNameMap)
          (
            None,
            None,
            Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM | Flag.BYNAMEPARAM} val $f: $codomain = $EmptyTree"),
            q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} val $tname: $tpt = $f"
          )

        case q"$mods var $tname: $tpt = ${`EmptyTree`}" =>
          val f = TermName(c.freshName("f"))
          val codomain = modifyType(tpt, typeNameMap)
          (
            None,
            None,
            Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM | Flag.BYNAMEPARAM} val $f: $codomain = $EmptyTree"),
            q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} var $tname: $tpt = $f"
          )

        case stat => (None, None, None, stat)
      }

      (
        res.collect { case (Some(r), _, _, _) => r },
        res.collect { case (_, Some(r), _, _) => r },
        res.collect { case (_, _, Some(r), _) => r },
        res.map(_._4)
      )
    }

    def createInstanceMethod(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Tree = {
      val typeNameMap = createTypeNameMap(stats)
      val (tparams1, typs, params, stats1) = modifyStats(stats, typeNameMap)
      val tparams2 = modifyTparams(tparams)._2
      q"def instance[..${tparams ++ tparams1}](...${Seq(params)}): $tpname[..$tparams2] { ..$typs } = new $tpname[..$tparams2] { ..$stats1 }"
    }

    def createObject(name: TermName, earlydefns: Seq[Tree], parents: Seq[Tree], self: Tree, tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree], body: Seq[Tree]): Tree =
      q"""
         object $name extends { ..$earlydefns } with ..$parents { $self =>
           ${createInstanceMethod(tparams, tpname, stats)}
           ..$body
         }
       """

    def createBlock(trt: Tree, name: TermName, earlydefns: Seq[Tree], parents: Seq[Tree], self: Tree, tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree], body: Seq[Tree]): Tree =
      q"""
          $trt
          ${createObject(name, earlydefns, parents, self, tparams, tpname, stats, body)}
        """

    annottees match {
      case (trt @ q"$mods1 trait $tpname[..$tparams] extends { ..$earlydefns1 } with ..$parents1 { $self1 => ..$stats }") ::
        q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" :: Nil =>
        createBlock(trt, tname, earlydefns, parents, self, tparams, tpname, stats, body)

      case (trt @ q"$mods1 trait $tpname[..$tparams] extends { ..$earlydefns1 } with ..$parents1 { $self1 => ..$stats }") :: Nil =>
        createBlock(trt, tpname.toTermName, Seq(), Seq(tq"_root_.scala.AnyRef"), q"val ${TermName(c.freshName("self"))} = $EmptyTree", tparams, tpname, stats, Seq())

      case _ => c.abort(c.enclosingPosition, "not trait")
    }
  }
}

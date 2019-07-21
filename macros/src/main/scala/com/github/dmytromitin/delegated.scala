package com.github.dmytromitin

import macrocompat.bundle
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class delegated extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro DelegatedMacro.impl
}

@bundle
class DelegatedMacro(val c: whitebox.Context) extends Helpers {
  import c.universe._

  def impl(annottees: Tree*): Tree = {
    def modifyName(name: TypeName): TypeName = name match {
      case TypeName("_") => TypeName(c.freshName("tparam"))
      case _ => name
    }

    def modifyTparam(tparam: Tree): (TypeDef, Tree) = {
      tparam match {
        case q"$mods type $name[..$tparams] >: $low <: $high" =>
          val name1 = modifyName(name)
          (
            q"$mods type $name1[..$tparams] >: $low <: $high",
            tq"$name1"
          )
      }
    }

    def modifyTparams(tparams: Seq[Tree]): (Seq[TypeDef], Seq[Tree]) = {
      val res = tparams.map(modifyTparam(_))
      (res.map(_._1), res.map(_._2))
    }

    def modifyParam(param: Tree): /*(Tree,*/ Tree/*)*/ = param match {
      case q"$mods val $tname: $tpt = $expr" => /*(tpt,*/ q"$tname"/*)*/
    }

    def modifyParamss(paramss: Seq[Seq[Tree]]): /*(Seq[Seq[Tree]],*/ Seq[Seq[Tree]]/*)*/ = {
      val res = paramss.map(_.map(modifyParam))
      /*(res.map(_.map(_._1)),*/ res/*.map(_.map(_._2))*//*)*/
    }

    def createTypeNameSet(stats: Seq[Tree]): Set[TypeName] =
      stats.collect {
        case q"$mods type $name[..$tparams] >: $low <: $high" => name
      }.toSet

    def modifyType(tpt: Tree, typeNameSet: Set[TypeName], inst: TermName): Tree = {
      val transformer = new Transformer {
        override def transform(tree: Tree): Tree = tree match {
          case tq"${name: TypeName}" => if (typeNameSet(name)) tq"$inst.$name" else tq"$name"
          case _ => super.transform(tree)
        }
      }

      transformer.transform(tpt)
    }

    def modifyStat(tparams: Seq[TypeDef], tpname: TypeName, typeNameSet: Set[TypeName]): PartialFunction[Tree, Tree] = {
      val inst = TermName(c.freshName("inst"))

      {
        case q"${mods: Modifiers} def $tname[..$methodTparams](...$paramss): $tpt = ${`EmptyTree`}" =>
          val tparams1 = modifyTparams(tparams)
          val methodTparams1 = modifyTparams(methodTparams)
          val paramNamess = modifyParamss(paramss)
          val tpt1 = modifyType(tpt, typeNameSet, inst)
          // TODO paramss can already have implicits
          q"${mods & ~Flag.DEFERRED} def $tname[..${tparams1._1 ++ methodTparams}](...$paramss)(implicit $inst: $tpname[..${tparams1._2}]): $tpt1 = $inst.$tname[..${methodTparams1._2}](...$paramNamess)"
      }
    }

    def createDelegatingMethods(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Seq[Tree] = {
      val typeNameSet = createTypeNameSet(stats)
      stats.collect(modifyStat(tparams, tpname, typeNameSet))
    }

    def createObject(name: TermName, earlydefns: Seq[Tree], parents: Seq[Tree], self: Tree, tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree], body: Seq[Tree]): Tree =
      q"""
         object $name extends { ..$earlydefns } with ..$parents { $self =>
           ..${createDelegatingMethods(tparams, tpname, stats)}
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
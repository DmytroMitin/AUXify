package com.github.dmytromitin.auxify.macros

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

    def modifyTyp(mods: Modifiers, name: TypeName, tparams: Seq[TypeDef], low: Tree, high: Tree, typeNameMap: Map[TypeName, TypeName]): (Option[TypeDef], Option[TypeDef], Option[Tree], Tree) = {
      val name0 = typeNameMap(name)
      val modifiedTparams = modifyTparams(tparams)
      def mkStat(ms: Modifiers) = q"$ms type $name[..${modifiedTparams._1}] = $name0[..${modifiedTparams._2}]"
      (
        Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM} type $name0[..$tparams] >: $low <: $high"),
        Some(mkStat(mods & ~Flag.DEFERRED)),
        None,
        mkStat(mods & ~Flag.DEFERRED | Flag.OVERRIDE)
      )
    }

    def modifyDef(mods: Modifiers, tname: TermName, paramss: Seq[Seq[Tree]], typeNameMap: Map[TypeName, TypeName], tpt: Tree): (Option[TypeDef], Option[TypeDef], Option[Tree], Tree) = {
      val f = TermName(c.freshName("f"))
      val (paramTypess, paramNamess) = modifyParamss(paramss)
      val domain = paramTypess.map(_.map(modifyType(_, typeNameMap)))
      val codomain = modifyType(tpt, typeNameMap)
      val functionType = createFunctionType(domain, codomain)
      (
        None,
        None,
        Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM} val $f: $functionType = $EmptyTree"),
        q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} def $tname[..${Seq[Tree]()}](...$paramss): $tpt = $f(...$paramNamess)"
      )
    }

    def modifyVal(mods: Modifiers, tname: TermName, tpt: Tree, typeNameMap: Map[TypeName, TypeName]): (Option[TypeDef], Option[TypeDef], Option[Tree], Tree) = {
      val f = TermName(c.freshName("f"))
      val codomain = modifyType(tpt, typeNameMap)
      (
        None,
        None,
        Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM | Flag.BYNAMEPARAM} val $f: $codomain = $EmptyTree"),
        q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} val $tname: $tpt = $f"
      )
    }

    def modifyVar(mods: Modifiers, tname: TermName, tpt: Tree, typeNameMap: Map[TypeName, TypeName]): (Option[TypeDef], Option[TypeDef], Option[Tree], Tree) = {
      val f = TermName(c.freshName("f"))
      val codomain = modifyType(tpt, typeNameMap)
      (
        None,
        None,
        Some(q"${mods & ~Flag.DEFERRED | Flag.PARAM | Flag.BYNAMEPARAM} val $f: $codomain = $EmptyTree"),
        q"${mods & ~Flag.DEFERRED | Flag.OVERRIDE} var $tname: $tpt = $f"
      )
    }

    def modifyStats(stats: Seq[Tree], typeNameMap: Map[TypeName, TypeName]): (Seq[TypeDef], Seq[TypeDef], Seq[Tree], Seq[Tree]) = {
      val res: Seq[(Option[TypeDef], Option[TypeDef], Option[Tree], Tree)] = stats.map {
        case q"${mods: Modifiers} type $name[..$tparams] >: $low <: $high" =>
          modifyTyp(mods, name, tparams, low, high, typeNameMap)

        case q"${mods: Modifiers} def $tname[..$tparams](...$paramss): $tpt = ${`EmptyTree`}" =>
          if (tparams.isEmpty) modifyDef(mods, tname, paramss, typeNameMap, tpt)
          else c.abort(c.enclosingPosition, "polymorphic method")

        case q"$mods val $tname: $tpt = ${`EmptyTree`}" => modifyVal(mods, tname, tpt, typeNameMap)

        case q"$mods var $tname: $tpt = ${`EmptyTree`}" => modifyVar(mods, tname, tpt, typeNameMap)

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

    modifyAnnottees(annottees, (tparams, tpname, stats) => Seq(createInstanceMethod(tparams, tpname, stats)))
  }
}

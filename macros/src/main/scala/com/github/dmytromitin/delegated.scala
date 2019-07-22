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
    def modifyStat(tparams: Seq[TypeDef], tpname: TypeName, typeNameSet: Set[TypeName]): PartialFunction[Tree, Tree] = {
      case q"${mods: Modifiers} def $tname[..$methodTparams](...$paramss): $tpt = ${`EmptyTree`}" =>
        val inst = TermName(c.freshName("inst"))
        val tparams1 = modifyTparams(tparams)
        val methodTparams1 = modifyTparams(methodTparams)
        val paramNamess = modifyParamss(paramss)._2
        val tpt1 = modifyType(tpt, typeNameSet, inst)
        val implct = q"implicit val $inst: $tpname[..${tparams1._2}]"
        q"${mods & ~Flag.DEFERRED} def $tname[..${tparams1._1 ++ methodTparams}](...${addImplicitToParamss(paramss, implct)}): $tpt1 = $inst.$tname[..${methodTparams1._2}](...$paramNamess)"
    }

    def createDelegatingMethods(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Seq[Tree] = {
      val typeNameSet = createTypeNameSet(stats)
      stats.collect(modifyStat(tparams, tpname, typeNameSet))
    }

    modifyAnnottees(annottees, (tparams, tpname, stats) => createDelegatingMethods(tparams, tpname, stats))
  }
}
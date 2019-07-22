package com.github.dmytromitin

import macrocompat.bundle
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class apply extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ApplyMacro.impl
}

@bundle
class ApplyMacro(val c: whitebox.Context) extends Helpers {
  import c.universe._

  def impl(annottees: Tree*): Tree = {
    def createApply(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Tree = {
      val typs = extractTypeMembers(stats)._3
      val tparams2 = modifyTparams(tparams)._2
      q"def apply[..$tparams](implicit inst: $tpname[..$tparams2]): $tpname[..$tparams2] { ..$typs } = inst"
    }

    modifyAnnottees(annottees, (tparams, tpname, stats) => Seq(createApply(tparams, tpname, stats)))
  }
}
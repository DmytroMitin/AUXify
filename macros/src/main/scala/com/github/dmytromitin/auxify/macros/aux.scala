package com.github.dmytromitin.auxify.macros

import macrocompat.bundle
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class aux extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro AuxMacro.impl
}

@bundle
class AuxMacro(val c: whitebox.Context) extends Helpers {
  import c.universe._

  def impl(annottees: Tree*): Tree = {
    def createAux(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Tree = {
      val (tparams1, typs, _, _) = extractTypeMembers(stats)
      val tparams2 = modifyTparams(tparams)._2
      q"type Aux[..${tparams ++ tparams1}] = $tpname[..$tparams2] { ..$typs }"
    }

    modifyAnnottees(annottees, (tparams, tpname, stats) => Seq(createAux(tparams, tpname, stats)))
  }
}

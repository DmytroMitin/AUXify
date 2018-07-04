package com.github.dmytromitin

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Aux extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro AuxMacro.impl
}

object AuxMacro {
  def impl(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._

    def modifyTparams(tparams: Seq[Tree]): Seq[Tree] = tparams.map {
      case q"$_ type $name[..$tparams] >: $_ <: $_" => tq"$name[..$tparams]"
    }

    def extractTyps(stats: Seq[Tree]): (Seq[TypeDef], Seq[TypeDef]) = {
      val typs = stats.collect {
        case q"$_ type $name[..$tparams] >: $low <: $high" =>
          val name0 = TypeName(name.toString + "0")
          (q"${Modifiers()} type $name0[..$tparams] >: $low <: $high",
            q"${Modifiers()} type $name = $name0")
      }

      (typs.map(_._1), typs.map(_._2))
    }

    annottees match {
      case (trt @ q"$_ trait $tpname[..$tparams] extends { ..$_ } with ..$_ { $_ => ..$stats }") ::
        q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" :: Nil =>

        val (tparams1, typs) = extractTyps(stats)
        val tparams2 = modifyTparams(tparams)

        q"""
            $trt
            $mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
              type Aux[..${tparams ++ tparams1}] = $tpname[..$tparams2] { ..$typs }
              ..$body
            }
          """

      case (trt @ q"$_ trait $tpname[..$tparams] extends { ..$_ } with ..$_ { $_ => ..$stats }") :: Nil =>
        val (tparams1, typs) = extractTyps(stats)
        val tparams2 = modifyTparams(tparams)

        q"""
            $trt
            object ${tpname.toTermName} {
              type Aux[..${tparams ++ tparams1}] = $tpname[..$tparams2] { ..$typs }
            }
          """
    }
  }
}

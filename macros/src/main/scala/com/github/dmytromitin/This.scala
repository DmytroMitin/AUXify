package com.github.dmytromitin

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class This extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro thisMacro.impl
}

object thisMacro {
  def impl(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._
    annottees match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        val tparams1 = tparams.map {
          case q"$_ type $name[..$_] >: $_ <: $_" => tq"$name"
        }
        q"""
            $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self =>
              type This = $tpname[..$tparams1]
              ..$stats
            }

            ..$tail
          """

      case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" :: Nil =>
        q"""
            $mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
              type This = $tname.type
              ..$body
            }
          """

      case _ => c.abort(c.enclosingPosition, "not class or object")
    }

  }
}
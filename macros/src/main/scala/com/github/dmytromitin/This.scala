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

    def modifyTparams(tparams: Seq[Tree]): Seq[Tree] = tparams.map {
      case q"$_ type $name[..$tparams] >: $_ <: $_" => tq"$name"
    }

    annottees match {
      case q"$mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        val self1 = self match {
          case q"$mods val ${TermName("_")}: $tpt = $expr" =>
            q"$mods val ${TermName(c.freshName("self"))}: $tpt = $expr"
          case _ => self
        }
        val self2 = self1 match {
          case q"$_ val $selfName: $_ = $_" => selfName
        }
        q"""
            $mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self1 =>
              type This >: this.type <: $tpname[..${modifyTparams(tparams)}] { type This = $self2.This }
              ..$stats
            }

            ..$tail
          """
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        q"""
            $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self =>
              type This = $tpname[..${modifyTparams(tparams)}]
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

      case _ => c.abort(c.enclosingPosition, "not trait, class or object")
    }

  }
}
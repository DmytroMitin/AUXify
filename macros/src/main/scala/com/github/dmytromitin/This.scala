package com.github.dmytromitin

import macrocompat.bundle

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class This(lowerBound: Boolean = true, fBound: Boolean = true) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ThisMacro.impl
}

@bundle
class ThisMacro(val c: whitebox.Context) {
  import c.universe._

  def impl(annottees: Tree*): Tree = {
    val (isLowerBoundOn, isFBoundOn) = c.prefix.tree match {
      case q"new This(lowerBound = ${lb: Boolean}, fBound     = ${fb: Boolean})" => (lb,   fb)
      case q"new This(fBound     = ${fb: Boolean}, lowerBound = ${lb: Boolean})" => (lb,   fb)
      case q"new This(fBound     = ${fb: Boolean}                             )" => (true, fb)
      case q"new This(lowerBound = ${lb: Boolean}                             )" => (lb,   true)
      case q"new This(lowerBound = ${lb: Boolean},              ${fb: Boolean})" => (lb,   fb)
      case q"new This(             ${lb: Boolean}, fBound     = ${fb: Boolean})" => (lb,   fb)
      case q"new This(             ${lb: Boolean},              ${fb: Boolean})" => (lb,   fb)
      case q"new This(             ${lb: Boolean}                             )" => (lb,   true)
      case q"new This(                                                        )" => (true, true)
      case _ => c.abort(c.enclosingPosition, "not boolean literal")
    }

    def modifyTparams(tparams: Seq[Tree]): Seq[Tree] = tparams.map {
      case q"$mods type $name[..$tparams] >: $lower <: $higher" => tq"$name"
    }

    annottees match {
      case q"$mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        val self1 = self match {
          case q"$mods val ${TermName("_")}: $tpt = $expr" =>
            q"$mods val ${TermName(c.freshName("self"))}: $tpt = $expr"
          case _ => self
        }
        val self2 = self1 match {
          case q"$mods val $selfName: $tpt = $expr" => selfName
        }
        val lowerBound = if (isLowerBoundOn) tq"this.type" else tq"_root_.scala.Nothing"
        val fBound = if (isFBoundOn) Seq(q"type This = $self2.This") else Seq[Tree]()
        q"""
            $mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self1 =>
              type This >: $lowerBound <: $tpname[..${modifyTparams(tparams)}] { ..$fBound }
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
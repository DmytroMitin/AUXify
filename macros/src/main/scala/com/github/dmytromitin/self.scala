package com.github.dmytromitin

import macrocompat.bundle
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class self(lowerBound: Boolean = true, fBound: Boolean = true) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro SelfMacro.impl
}

@bundle
class SelfMacro(val c: whitebox.Context) extends Helpers {
  import c.universe._

  def impl(annottees: Tree*): Tree = {
    val (isLowerBoundOn, isFBoundOn) = c.prefix.tree match {
      case q"new self(lowerBound = ${lb: Boolean}, fBound     = ${fb: Boolean})" => (lb,   fb)
      case q"new self(fBound     = ${fb: Boolean}, lowerBound = ${lb: Boolean})" => (lb,   fb)
      case q"new self(fBound     = ${fb: Boolean}                             )" => (true, fb)
      case q"new self(lowerBound = ${lb: Boolean}                             )" => (lb,   true)
      case q"new self(lowerBound = ${lb: Boolean},              ${fb: Boolean})" => (lb,   fb)
      case q"new self(             ${lb: Boolean}, fBound     = ${fb: Boolean})" => (lb,   fb)
      case q"new self(             ${lb: Boolean},              ${fb: Boolean})" => (lb,   fb)
      case q"new self(             ${lb: Boolean}                             )" => (lb,   true)
      case q"new self(                                                        )" => (true, true)
      case _ => c.abort(c.enclosingPosition, "not boolean literal")
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
        val fBound = if (isFBoundOn) Seq(q"type Self = $self2.Self") else Seq[Tree]()
        q"""
            $mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self1 =>
              type Self >: $lowerBound <: $tpname[..${modifyTparams(tparams)._2}] { ..$fBound }
              ..$stats
            }

            ..$tail
          """

      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        q"""
            $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self =>
              type Self = $tpname[..${modifyTparams(tparams)._2}]
              ..$stats
            }

            ..$tail
          """

      case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" :: Nil =>
        q"""
            $mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
              type Self = $tname.type
              ..$body
            }
          """

      case _ => c.abort(c.enclosingPosition, "not trait, class or object")
    }

  }
}
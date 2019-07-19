package com.github.dmytromitin

import macrocompat.bundle
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class Aux extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro AuxMacro.impl
}

@bundle
class AuxMacro(val c: whitebox.Context) {
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

    def extractTyps(stats: Seq[Tree]): (Seq[TypeDef], Seq[TypeDef]) = {
      val typs = stats.collect {
        case q"$mods type $name[..$tparams] >: $low <: $high" =>
          val name0 = TypeName(name.toString + "0")
          val modifiedTparams = modifyTparams(tparams)
          (
            q"${Modifiers(Flag.PARAM)} type $name0[..$tparams] >: $low <: $high",
            q"${Modifiers()} type $name[..${modifiedTparams._1}] = $name0[..${modifiedTparams._2}]"
          )
      }

      (typs.map(_._1), typs.map(_._2))
    }

    def createAux(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Tree = {
      val (tparams1, typs) = extractTyps(stats)
      val tparams2 = modifyTparams(tparams)._2
      q"type Aux[..${tparams ++ tparams1}] = $tpname[..$tparams2] { ..$typs }"
    }

    def createObject(name: TermName, earlydefns: Seq[Tree], parents: Seq[Tree], self: Tree, tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree], body: Seq[Tree]): Tree =
      q"""
         object $name extends { ..$earlydefns } with ..$parents { $self =>
           ${createAux(tparams, tpname, stats)}
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

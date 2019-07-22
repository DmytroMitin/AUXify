package com.github.dmytromitin

import macrocompat.bundle
import scala.reflect.macros.whitebox

@bundle
trait Helpers {
  val c: whitebox.Context

  import c.universe._

  implicit class ModifiersOps(left: Modifiers) {
    def & (right: FlagSet): Modifiers = left match {
      case Modifiers(flags, privateWithin, annots) => Modifiers(flags & right, privateWithin, annots)
    }
    def | (right: FlagSet): Modifiers = left match {
      case Modifiers(flags, privateWithin, annots) => Modifiers(flags | right, privateWithin, annots)
    }
  }

  implicit class FlagSetOps(left: FlagSet) {
    def & (right: FlagSet): FlagSet = (left.asInstanceOf[Long] & right.asInstanceOf[Long]).asInstanceOf[FlagSet]
    def unary_~ : FlagSet = (~ left.asInstanceOf[Long]).asInstanceOf[FlagSet]
  }

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

  def extractTyps(stats: Seq[Tree]): (Seq[TypeDef], Seq[TypeDef], Seq[TypeDef]) = {
    val typs = stats.collect {
      case q"$mods type $name[..$tparams] >: $low <: $high" =>
        val name0 = TypeName(c.freshName(name.toString + "0"))
        val modifiedTparams = modifyTparams(tparams)
        (
          q"${Modifiers(Flag.PARAM)} type $name0[..$tparams] >: $low <: $high",
          q"${Modifiers()} type $name[..${modifiedTparams._1}] = $name0[..${modifiedTparams._2}]",
          q"${Modifiers()} type $name[..${modifiedTparams._1}] = inst.$name[..${modifiedTparams._2}]"
        )
    }

    (typs.map(_._1), typs.map(_._2), typs.map(_._3))
  }

  def modifyParam(param: Tree): (Tree, Tree) = param match {
    case q"$mods val $tname: $tpt = $expr" => (tpt, q"$tname")
  }

  def modifyParamss(paramss: Seq[Seq[Tree]]): (Seq[Seq[Tree]], Seq[Seq[Tree]]) = {
    val res = paramss.map(_.map(modifyParam))
    (res.map(_.map(_._1)), res.map(_.map(_._2)))
  }

  def createTypeNameSet(stats: Seq[Tree]): Set[TypeName] =
    createTypeNameMap(stats).keySet

  def createTypeNameMap(stats: Seq[Tree]): Map[TypeName, TypeName] =
    stats.collect {
      case q"$mods type $name[..$tparams] >: $low <: $high" =>
        val name0 = TypeName(c.freshName(name.toString + "0"))
        name -> name0
    }.toMap

  def modifyType(tpt: Tree, typeNameMap: Map[TypeName, TypeName]): Tree =
    modifyTypeWithTransformer(tpt, (name: TypeName) => tq"${typeNameMap.applyOrElse(name, identity[TypeName])}")

  def modifyType(tpt: Tree, typeNameSet: Set[TypeName], inst: TermName): Tree =
    modifyTypeWithTransformer(tpt, (name: TypeName) => if (typeNameSet(name)) tq"$inst.$name" else tq"$name")

  def modifyTypeWithTransformer(tpt: Tree, f: TypeName => Tree): Tree = {
    val transformer = new Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case tq"${name: TypeName}" => f(name)
        case _ => super.transform(tree)
      }
    }

    transformer.transform(tpt)
  }

}

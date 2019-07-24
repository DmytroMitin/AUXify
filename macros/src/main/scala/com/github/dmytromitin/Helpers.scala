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

  def modifyTypeDef(mods: Modifiers, name: TypeName, tparams: Seq[TypeDef], low: Tree, high: Tree): (TypeDef, TypeDef, TypeDef, (TypeName, TypeName)) = {
    val name0 = TypeName(c.freshName(name.toString + "0"))
    val modifiedTparams = modifyTparams(tparams)
    def mkTyp(t: Tree) = q"${mods & ~Flag.DEFERRED} type $name[..${modifiedTparams._1}] = $t[..${modifiedTparams._2}]"
    (
      q"${mods & ~Flag.DEFERRED | Flag.PARAM} type $name0[..$tparams] >: $low <: $high",
      mkTyp(tq"$name0"),
      mkTyp(tq"inst.$name"),
      name -> name0
    )
  }

  def modifyStat: PartialFunction[Tree, (TypeDef, TypeDef, TypeDef, (TypeName, TypeName))] = {
    case q"${mods: Modifiers} type $name[..$tparams] >: $low <: $high" =>
      modifyTypeDef(mods, name, tparams, low, high)
  }

  def extractTypeMembers(stats: Seq[Tree]): (Seq[TypeDef], Seq[TypeDef], Seq[TypeDef], Seq[(TypeName, TypeName)]) = {
    val typs = stats.collect(modifyStat)
    (typs.map(_._1), typs.map(_._2), typs.map(_._3), typs.map(_._4))
  }

  def createTypeNameMap(stats: Seq[Tree]): Map[TypeName, TypeName] = extractTypeMembers(stats)._4.toMap

  def createTypeNameSet(stats: Seq[Tree]): Set[TypeName] = createTypeNameMap(stats).keySet

  def modifyParam(param: Tree): (Tree, Tree) = param match {
    case q"$mods val $tname: $tpt = $expr" => (tpt, q"$tname")
  }

  def modifyParamss(paramss: Seq[Seq[Tree]]): (Seq[Seq[Tree]], Seq[Seq[Tree]]) = {
    val res = paramss.map(_.map(modifyParam))
    (res.map(_.map(_._1)), res.map(_.map(_._2)))
  }

  def modifyType(tpt: Tree, typeNameMap: Map[TypeName, TypeName]): Tree =
    modifyTypeWithTransformer(tpt, name => tq"${typeNameMap.applyOrElse(name, identity[TypeName])}")

  def modifyType(tpt: Tree, typeNameSet: Set[TypeName], inst: TermName): Tree =
    modifyTypeWithTransformer(tpt, name => if (typeNameSet(name)) tq"$inst.$name" else tq"$name")

  def modifyTypeWithTransformer(tpt: Tree, f: TypeName => Tree): Tree = {
    val transformer = new Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case tq"${name: TypeName}" => f(name)
        case _ => super.transform(tree)
      }
    }

    transformer.transform(tpt)
  }

  def addImplicitToParamss(paramss: Seq[Seq[Tree]], implct: Tree): Seq[Seq[Tree]] = {
    val default = paramss :+ Seq(implct)
    if (paramss.isEmpty) default
    else {
      val last = paramss.last
      if (last.isEmpty) default
      else last.head match {
        case q"${mods: Modifiers} val $tname: $tpt = $expr" =>
          if (mods hasFlag Flag.IMPLICIT)
            paramss.init :+ (last :+ implct)
          else default
      }
    }
  }

  def modifyAnnottees(annottees: Seq[Tree], f: (Seq[TypeDef], TypeName, Seq[Tree]) => Seq[Tree]): Tree = {
    def createObject(name: TermName, earlydefns: Seq[Tree], parents: Seq[Tree], self: Tree, tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree], body: Seq[Tree]): Tree =
      q"""
         object $name extends { ..$earlydefns } with ..$parents { $self =>
           ..${f(tparams, tpname, stats)}
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

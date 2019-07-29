package com.github.dmytromitin.auxify.meta.syntactic

import scala.meta._

object ScalametaTransformer {
  def freshName(prefix: String): Type.Name = Type.fresh(prefix + "$meta$")

  val isAux: Mod => Boolean = { case mod"@aux" => true; case _ => false }
  val isVariant: Mod => Boolean = { case mod"+" => true; case mod"-" => true; case _ => false }

  def modifyName(tname: Name): Type.Name = tname match {
    case Name.Anonymous() => freshName("tparam")
    case tn: Type.Name => tn
  }

  def prepareForMethod(tparam: Type.Param): (Type.Param, Type) =
    tparam match {
      case tparam"..$mods $tname[..$tparams] >: $low <: $high <% ..$view : ..$ctxt" =>
        val tname1 = modifyName(tname)
        (
          tparam"..${mods.filterNot(isVariant)} $tname1[..$tparams] >: $low <: $high",
          t"$tname1"
        )
    }

  def prepareForMethod(tparams: List[Type.Param]): (List[Type.Param], List[Type]) = {
    val res = tparams.map(prepareForMethod)
    (res.map(_._1), res.map(_._2))
  }

  def createTypeNameMap(stats: List[Stat]): Map[String, Type.Name] = {
    stats.collect {
      case q"..$_ type $tname[..$_] >: $_ <: $_" =>
        val str = tname.toString
        str -> freshName(str + "0")
    }.toMap
  }

  def extractTypeMembers(stats: List[Stat], typeNameMap: Map[String, Type.Name]): (List[Type.Param], List[Stat]) = {
    val typs = stats.collect(modifyStat(typeNameMap))
    (typs.map(_._1), typs.map(_._2))
  }

  def renameByMap(typ: Type, typeNameMap: Map[String, Type.Name]): Type = {
    val transformer = new Transformer {
      override def apply(tree: Tree): Tree = tree match {
        case n: Type.Name => typeNameMap.getOrElse(n.value, n)
        case t => super.apply(t)
      }
    }

    transformer(typ) match { case tp: Type => tp }
  }

  def renameByMap(typ: Option[Type], typeNameMap: Map[String, Type.Name]): Option[Type] = typ.map(renameByMap(_, typeNameMap))

  def modifyStat(typeNameMap: Map[String, Type.Name]): PartialFunction[Stat, (Type.Param, Stat)] = {
    case q"..$mods type $tname[..$tparams] >: $low <: $high" =>
      val tname0 = typeNameMap(tname.value)
      val (tparams1, types) = prepareForMethod(tparams)
      val rhs = if (types.isEmpty) t"$tname0" else t"$tname0[..$types]"
      (
        tparam"..$mods $tname0[..$tparams] >: ${renameByMap(low, typeNameMap)} <: ${renameByMap(high, typeNameMap)}",
        q"..$mods type $tname[..$tparams1] = $rhs",
      )
  }

  def createAux(tparams: List[Type.Param], tpname: Type.Name, stats: List[Stat]): Stat = {
    val typeNameMap = createTypeNameMap(stats)
    val (tparams1, stats1) = extractTypeMembers(stats, typeNameMap)
    val (tparams2, types) = prepareForMethod(tparams)
    val rhs = if (types.isEmpty) t"$tpname { ..$stats1 }" else t"$tpname[..$types] { ..$stats1 }"
    q"type Aux[..${tparams2 ++ tparams1}] = $rhs"
  }

  def transform(stats: List[Stat]): List[Stat] = {
    val tnames = stats.collect {
      case q"..$mods class $tname[..$tparams] ..$_ (...$_) extends { ..$_ } with ..$_ { $_ => ..$stats }" if mods.exists(isAux) =>
        tname.value -> (tparams, stats)
    }.toMap

    stats.map {
      case q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template" if mods.exists(isAux) =>
        q"..${mods.filterNot(isAux)} class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template"
      case q"..$mods object $ename extends { ..$stats } with ..$inits { $self => ..$stats1 }" if tnames.contains(ename.value) =>
        q"..$mods object $ename extends { ..$stats } with ..$inits { $self => ..${createAux(tnames(ename.value)._1, Type.Name(ename.value), tnames(ename.value)._2) :: stats1} }"
      case t => t
    }
  }

  def transform(tree: Tree): Tree = {
    val transformer = new Transformer {
      override def apply(tree: Tree): Tree = tree match {
        case template"{ ..$stats } with ..$inits { $self => ..$stats1 }"  =>
          template"{ ..${transform(stats)} } with ..$inits { $self => ..${transform(stats1)} }"
        case q"{ ..$stats }" =>
          q"{ ..${transform(stats)} }"
//        case q"new { ..$stat } with ..$inits { $self => ..$stats }" =>
//          q"new { ..$stat } with ..$inits { $self => ..${transform(stats)} }"
        case t"$tpeopt { ..$stats }" =>
          t"$tpeopt { ..${transform(stats)} }"
        case t"$tpe forSome { ..$statsnel }" =>
          t"$tpe forSome { ..${transform(statsnel)} }"
        case q"package $eref { ..$stats }" =>
          q"package $eref { ..${transform(stats)} }"
//        case q"..$stats" =>
//          q"..${transform(stats)}"
//        case source"..$stats" => // TODO without this top-level doesn't work, with this nested doesn't work #23
//          source"..${transform(stats)}"
        case t =>
          super.apply(t)
      }
    }

    transformer(tree)
  }

  def transform(input: String): String = transform(input.parse[Source].get).toString
}

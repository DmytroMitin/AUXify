package com.github.dmytromitin.auxify.meta

import scalafix.v1._
import scala.meta._

class AuxRule extends SemanticRule("AuxRule") {
  def freshName(prefix: String): Type.Name = Type.fresh(prefix + "$meta$")

  override def description: String = "Expand annotation @aux"

  override def isRewrite: Boolean = true

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
        q"..$mods type $tname[..$tparams1] = $rhs"
      )
  }

  def createAux(tparams: List[Type.Param], tpname: Type.Name, stats: List[Stat]): Stat = {
    val typeNameMap = createTypeNameMap(stats)
    val (tparams1, stats1) = extractTypeMembers(stats, typeNameMap)
    val (tparams2, types) = prepareForMethod(tparams)
    val rhs = if (types.isEmpty) t"$tpname { ..$stats1 }" else t"$tpname[..$types] { ..$stats1 }"
    q"type Aux[..${tparams2 ++ tparams1}] = $rhs"
  }

  def addAuxToCompanion(symbol: Symbol, auxStr: String)(implicit doc: SemanticDocument): Patch = {
    val companion = SymbolMatcher.exact(symbol.owner + symbol.displayName + ".")
    doc.tree.collect {
      case companion(t) => t match {
        case q"..$_ object $_ extends $template" => template match {
          case template"{ ..$_ } with ..$_ { $_ => ..$stats }" if stats.nonEmpty =>
            Patch.addLeft(stats.head, auxStr)
          case _ =>
            Patch.addLeft(template.tokens.last, auxStr)
        }
        case _ => Patch.empty
      }
    }.asPatch
  }

  override def fix(implicit doc: SemanticDocument): Patch =
    doc.tree.collect {
      case tree @ q"..$mods class $tname[..$tparams] ..$_ (...$_) extends $template" if mods.exists(isAux) =>
        val removeAnnotation = Patch.replaceTree(mods.find(isAux).get, "")
        val aux = createAux(tparams, tname, template.stats)
        val modifyCompanion = addAuxToCompanion(tree.symbol, aux.toString + "\n")
        val createCompanion = Patch.addRight(tree, "\n" + q"object ${Term.Name(tname.toString)} { $aux }".toString)
        val createOrModifyCompanion = if (modifyCompanion.isEmpty) createCompanion else modifyCompanion
        removeAnnotation + createOrModifyCompanion
    }.asPatch
}

package com.github.dmytromitin

import macrocompat.bundle

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.collection.mutable
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise or -Ymacro-annotations")
class syntax extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro SyntaxMacro.impl
}

@bundle
class SyntaxMacro(val c: whitebox.Context) extends Helpers {
  import c.universe._

  def impl(annottees: Tree*): Tree = {

    def typeDependents(typ: Tree): Set[TypeName] = {
      val res = mutable.Set[TypeName]()

      val traverser = new Traverser {
        override def traverse(tree: Tree): Unit = tree match {
          case tq"${name: TypeName}" => res += name
          case _ => super.traverse(tree)
        }
      }

      traverser.traverse(typ)

      res.toSet
    }

    def firstParamTypeDependsOn(tparam: Tree, set: Set[TypeName]): Boolean = tparam match {
      case q"$mods type $tpname[..$tparams] = $tpt" => set.contains(tpname)
    }

    def mkRHS(tname: TermName, methodTparams: Seq[Tree], paramss: Seq[Seq[Tree]], inst: TermName): Tree = {
      val methodTparams1 = modifyTparams(methodTparams)
      val paramNamess = modifyParamss(paramss)._2
      q"$inst.$tname[..${methodTparams1._2}](...$paramNamess)"
    }

    def createImplicitClass(tparams: Seq[TypeDef], tpname: TypeName, typeNameSet: Set[TypeName]): PartialFunction[Tree, Tree] = {
      case q"${mods: Modifiers} def $tname[..$methodTparams](...$paramss): $tpt = ${`EmptyTree`}" if paramss.nonEmpty && paramss.head.nonEmpty =>
        val ops = TypeName(c.freshName("Ops"))
        val inst = TermName(c.freshName("inst"))
        val tpt1 = modifyType(tpt, typeNameSet, inst)

        val tparams1 = modifyTparams(tparams)
        val implct = q"implicit val $inst: $tpname[..${tparams1._2}]"
        val allTparams = tparams1._1 ++ methodTparams
        val (firstParam, restParamss) = (paramss.head.head, paramss.head.tail +: paramss.tail)
        val firstParamType = modifyParam(firstParam)._1
        val firstParamTypeDependents = typeDependents(firstParamType)
        val (firstTparams, restTparams) = allTparams.partition(firstParamTypeDependsOn(_, firstParamTypeDependents))
        val restParamssWithImplct = addImplicitToParamss(restParamss, implct)
        val rhs = mkRHS(tname, methodTparams, paramss, inst)

        q"""
           implicit class $ops[..$firstTparams]($firstParam) {
             ..${Seq(
               q"${mods & ~Flag.DEFERRED} def $tname[..$restTparams](...$restParamssWithImplct): $tpt1 = $rhs"
             )}
           }
         """

    }

    def createExtensionMethods(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Seq[Tree] = {
      val typeNameSet = createTypeNameSet(stats)
      stats.collect(createImplicitClass(tparams, tpname, typeNameSet))
    }

    modifyAnnottees(annottees, (tparams, tpname, stats) => Seq(
      q"""
         object syntax {
           ..${createExtensionMethods(tparams, tpname, stats)}
         }
       """
    ))
  }
}
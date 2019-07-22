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
    def modifyStat(tparams: Seq[TypeDef], tpname: TypeName, typeNameSet: Set[TypeName]): PartialFunction[Tree, Tree] = {
      case q"${mods: Modifiers} def $tname[..$methodTparams](...$paramss): $tpt = ${`EmptyTree`}" if paramss.nonEmpty && paramss.head.nonEmpty =>
        val inst = TermName(c.freshName("inst"))
        val ops = TypeName(c.freshName("Ops"))

        val tparams1 = modifyTparams(tparams)
        val methodTparams1 = modifyTparams(methodTparams)
        val paramNamess = modifyParamss(paramss)._2
        val tpt1 = modifyType(tpt, typeNameSet, inst)
        val implct = q"implicit val $inst: $tpname[..${tparams1._2}]"

        val allTparams: Seq[Tree] = tparams1._1 ++ methodTparams
        val firstParam: Tree = paramss.head.head
        val restParamss: Seq[Seq[Tree]] = paramss.head.tail +: paramss.tail
        val firstParamType: Tree = modifyParam(firstParam)._1

        val firstParamTypeDependents: Set[TypeName] = {
          val res = mutable.Set[TypeName]()

          val traverser = new Traverser {
            override def traverse(tree: Tree): Unit = tree match {
              case tq"${name: TypeName}" => res += name
              case _ => super.traverse(tree)
            }
          }

          traverser.traverse(firstParamType)

          res.toSet
        }

        def firstParamTypeDependsOn(tparam: Tree): Boolean = tparam match {
          case q"$mods type $tpname[..$tparams] = $tpt" => firstParamTypeDependents.contains(tpname)
        }

        val (firstTparams: Seq[Tree], restTparams: Seq[Tree]) = allTparams.partition(firstParamTypeDependsOn)

        q"""
           implicit class $ops[..$firstTparams]($firstParam) {
             ..${Seq(
               q"${mods & ~Flag.DEFERRED} def $tname[..$restTparams](...${addImplicitToParamss(restParamss, implct)}): $tpt1 = $inst.$tname[..${methodTparams1._2}](...$paramNamess)"
             )}
           }
         """

    }

    def createExtensionMethods(tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree]): Seq[Tree] = {
      val typeNameSet = createTypeNameSet(stats)
      stats.collect(modifyStat(tparams, tpname, typeNameSet))
    }

    def createObject(name: TermName, earlydefns: Seq[Tree], parents: Seq[Tree], self: Tree, tparams: Seq[TypeDef], tpname: TypeName, stats: Seq[Tree], body: Seq[Tree]): Tree =
      q"""
         object $name extends { ..$earlydefns } with ..$parents { $self =>
           object syntax {
             ..${createExtensionMethods(tparams, tpname, stats)}
           }
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
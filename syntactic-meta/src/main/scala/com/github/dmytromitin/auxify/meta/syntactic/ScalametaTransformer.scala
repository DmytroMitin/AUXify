package com.github.dmytromitin.auxify.meta.syntactic

import scala.meta._

object ScalametaTransformer {
  def transform(tree: Tree): Tree = {
    val isMain: Mod => Boolean = { case mod"@aux" => true; case _ => false }

    val transformer = new Transformer {
      override def apply(tree: Tree): Tree = tree match {
        case q"..$mods object $ename extends { ..$stats } with ..$inits { $self => ..$stats1 }" if mods.exists(isMain)  =>
          q"""
            ..${mods.filterNot(isMain)} object $ename extends { ..$stats } with ..$inits { $self =>
              def main(args: Array[String]): Unit = {
                ..$stats1
              }
            }"""

        case node => super.apply(node)
      }
    }

    transformer(tree)
  }

  def transform(input: String): String = transform(input.parse[Source].get).toString
  
}

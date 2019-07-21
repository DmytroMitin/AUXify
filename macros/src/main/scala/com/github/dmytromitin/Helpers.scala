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

}

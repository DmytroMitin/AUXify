package com.github.dmytromitin.auxify.shapeless

import macrocompat.bundle
import shapeless.SingletonTypeUtils
import scala.reflect.macros.whitebox

@bundle
class StringSymbolMacros(val c: whitebox.Context) extends SingletonTypeUtils {
  import c.universe.{Symbol => _, _}

  def stringToSymbolImpl(s: Tree): Tree = {
    SingletonSymbolType.unrefine(narrowedType(s)) match {
      case ConstantType(Constant(s: String)) => q"_root_.scala.Symbol.apply($s)"
      case typ => c.abort(c.enclosingPosition, s"stringToSymbol: unexpected type $typ=${showRaw(typ)}")
    }
  }

  def symbolToStringImpl(s: Tree): Tree = {
    SingletonSymbolType.unrefine(narrowedType(s)) match {
      case SingletonSymbolType(s) => q"$s"
      case typ => c.abort(c.enclosingPosition, s"symbolToString: unexpected type $typ=${showRaw(typ)}")
    }
  }

  private def narrowedType(t: Tree): Type =
    c.typecheck(q"""
      import _root_.shapeless.syntax.singleton._
      $t.narrow
    """).tpe

}
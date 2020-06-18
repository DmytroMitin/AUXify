package com.github.dmytromitin.auxify.shapeless

import shapeless.lens

//https://github.com/DmytroMitin/AUXify/issues/33
//https://stackoverflow.com/questions/50418767/shapeless-lenses-usage-with-a-string-definition/
class LensTest {
  case class Test(id: String, calc: Long)
  lens[Test] >> stringToSymbol("id")
}

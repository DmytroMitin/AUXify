package com.github.dmytromitin.auxify.meta.syntactic

import sbt._

object Generator {
  def gen(inputDir: File, outputDir: File): Seq[File] = {
    val finder: PathFinder = inputDir ** "*.scala"
    
    for(inputFile <- finder.get) yield {
      val inputStr = IO.read(inputFile)
      val outputFile = outputDir / inputFile.name
      val outputStr = ScalametaTransformer.transform(inputStr)
      IO.write(outputFile, outputStr)
      outputFile
    }
  }
}

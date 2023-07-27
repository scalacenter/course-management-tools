package com.lunatech.cmt.core.command

import sbt.io.syntax.File
import sbt.io.IO as sbtio

import java.nio.charset.StandardCharsets

object Package:

  def getCurrentExerciseId(bookmarkFile: File): String =
    sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head

  def starCurrentExercise(currentExercise: String, exercise: String): String =
    if (currentExercise == exercise) " * " else "   "

end Package

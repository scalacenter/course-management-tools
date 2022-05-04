package cmt

import sbt.io.syntax.*
object CMTAdmin:
  def renumberExercises(mainRepo: File,
                        renumOffset: Int,
                        renumStep: Int): Unit = 
    println(s"Renumbering exercises in ${toConsoleGreen(mainRepo.getPath)} starting at ${toConsoleGreen(renumOffset.toString)} with step size ${toConsoleGreen(renumStep.toString)}")

package cmt

import sbt.io.syntax.*
import sbt.io.{IO as sbtio}

import Helpers.{ExercisePrefixesAndExerciseNames, getExercisePrefixAndExercises, validatePrefixes}
object CMTAdmin:
  def renumberExercises(
      mainRepo: File,
      renumOffset: Int,
      renumStep: Int
  )(using config: CMTaConfig): Unit =
    
    val ExercisePrefixesAndExerciseNames(prefixes, exercises) = getExercisePrefixAndExercises(mainRepo)
    validatePrefixes(prefixes)
    val exercisePrefix = prefixes.head

    if renumOffset + (exercises.size - 1) * renumStep > 999 then
      printError(s"Cannot renumber exercises as it would exceed the available exercise number space")

    val moves =
      for {
        (exercise, index) <- exercises.zipWithIndex
        newNumber = renumOffset + index * renumStep
        oldExerciseFolder = mainRepo / config.mainRepoExerciseFolder / exercise
        newExerciseFolder = mainRepo / config.mainRepoExerciseFolder / renumberExercise(exercise, exercisePrefix, newNumber)
        if oldExerciseFolder != newExerciseFolder
      } yield (oldExerciseFolder, newExerciseFolder)
    sbtio.move(moves)

    if moves.size == 0 then
      printMessage("Renumber: nothing to do...")

    println(
      s"Renumbered exercises in ${toConsoleGreen(mainRepo.getPath)} starting at ${toConsoleGreen(
          renumOffset.toString
        )} with step size ${toConsoleGreen(renumStep.toString)}"
    )

  private val ExerciseNumberSpec = raw".*_(\d{3})_.*".r

  def extractExerciseNr(exercise: String): Int = {
    val ExerciseNumberSpec(d) = exercise : @unchecked
    d.toInt
  }
  private def renumberExercise(exercise: String, exercisePrefix: String, newNumber: Int): String =
    val newNumberLZ = f"${exercisePrefix}_$newNumber%03d_"
    val oldNumberPrefix = f"${exercisePrefix}_${extractExerciseNr(exercise)}%03d_"
    exercise.replaceFirst(oldNumberPrefix, newNumberLZ)


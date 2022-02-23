package cmt

import sbt.io.syntax.*
import sbt.io.IO as sbtio

import Helpers.{ExercisePrefixesAndExerciseNames, getExercisePrefixAndExercises, validatePrefixes, extractExerciseNr}
object CMTAdmin:
  def renumberExercises(mainRepo: File, renumStartAtOpt: Option[Int], renumOffset: Int, renumStep: Int)(using
      config: CMTaConfig): Unit =

    val ExercisePrefixesAndExerciseNames(prefixes, exercises) =
      getExercisePrefixAndExercises(mainRepo)
    validatePrefixes(prefixes)
    val exercisePrefix = prefixes.head

    val exerciseNumbers = exercises.map(extractExerciseNr)

    val renumStartAt =
      renumStartAtOpt match
        case None => exerciseNumbers.head
        case Some(num) =>
          if exerciseNumbers.contains(num)
          then num
          else
            printError(s"No exercise with number $num")
            0

    if exerciseNumbers.indexOf(renumStartAt) != 0 && renumOffset < renumStartAt then
      printError(s"Renumber offset ($renumOffset) cannot be smaller than Renumber start number ($renumStartAt)")

    if renumOffset + (exercises.size - exerciseNumbers.indexOf(renumStartAt) - 1) * renumStep > 999 then
      printError(s"Cannot renumber exercises as it would exceed the available exercise number space")

    val moves =
      for {
        (exercise, index) <- exercises.drop(exerciseNumbers.indexOf(renumStartAt)).zipWithIndex
        newNumber = renumOffset + index * renumStep
        oldExerciseFolder = mainRepo / config.mainRepoExerciseFolder / exercise
        newExerciseFolder =
          mainRepo / config.mainRepoExerciseFolder / renumberExercise(exercise, exercisePrefix, newNumber)
        if oldExerciseFolder != newExerciseFolder
      } yield (oldExerciseFolder, newExerciseFolder)
    sbtio.move(moves)

    if moves.isEmpty then
      printMessage("Renumber: nothing to do...")
      System.exit(0)

    println(toConsoleGreen(
      s"Renumbered exercises in ${mainRepo.getPath} starting at ${renumStartAt} to offset ${renumOffset.toString} with step size ${renumStep.toString}"))
  end renumberExercises

  def duplicateInsertBefore(mainRepo: File, exerciseNumber: Int): Unit =
    ???

  private def renumberExercise(exercise: String, exercisePrefix: String, newNumber: Int): String =
    val newNumberPrefix = f"${exercisePrefix}_$newNumber%03d_"
    val oldNumberPrefix =
      f"${exercisePrefix}_${extractExerciseNr(exercise)}%03d_"
    exercise.replaceFirst(oldNumberPrefix, newNumberPrefix)

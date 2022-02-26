package cmt

import sbt.io.syntax.*
import sbt.io.IO as sbtio

import Helpers.{ExercisePrefixesAndExerciseNames, getExercisePrefixAndExercises, validatePrefixes, extractExerciseNr}
object CMTAdmin:
  def renumberExercises(mainRepo: File, renumStartAtOpt: Option[Int], renumOffset: Int, renumStep: Int)(
      config: CMTaConfig): Either[String, Unit] =

    val ExercisePrefixesAndExerciseNames(prefixes, exercises) =
      getExercisePrefixAndExercises(mainRepo)(config)
    validatePrefixes(prefixes)
    val exercisePrefix = prefixes.head

    val exerciseNumbers = exercises.map(extractExerciseNr)
    val mainRepoExerciseFolder = mainRepo / config.mainRepoExerciseFolder

    val renumStartAt: Either[String, Int] =
      renumStartAtOpt match
        case None => Right(exerciseNumbers.head)
        case Some(num) =>
          if exerciseNumbers.contains(num)
          then Right(num)
          else Left(s"No exercise with number $num")

    renumStartAt.flatMap { renumStartAt =>
      val splitIndex = exerciseNumbers.indexOf(renumStartAt)
      val (exerciseNumsBeforeSplit, exerciseNumsAfterSplit) = exerciseNumbers.splitAt(splitIndex)
      val (_, exercisesAfterSplit) = exercises.splitAt(splitIndex)

      (exerciseNumsBeforeSplit, exerciseNumsAfterSplit) match
        case (Vector(), Vector(`renumOffset`, _)) =>
          Left("Renumber: nothing to renumber")
        case (before, _) if rangeOverlapsWithOtherExercises(before, renumOffset) =>
            Left("Moved exercise range overlaps with other exercises")
        case (before, _) if exceedsAvailableSpace(exercisesAfterSplit, renumOffset = renumOffset, renumStep= renumStep) =>
            Left(s"Cannot renumber exercises as it would exceed the available exercise number space")
        case (before, _) =>
          val moves =
            for {
              (exercise, index) <- exercisesAfterSplit.zipWithIndex
              newNumber = renumOffset + index * renumStep
              oldExerciseFolder = mainRepoExerciseFolder / exercise
              newExerciseFolder =
                mainRepoExerciseFolder / renumberExercise(exercise, exercisePrefix, newNumber)
              if oldExerciseFolder != newExerciseFolder
            } yield (oldExerciseFolder, newExerciseFolder)

          if moves.isEmpty
          then Left("Renumber: nothing to renumber")
          else
            sbtio.move(moves)
            Right(())
    }

  end renumberExercises

  private def exceedsAvailableSpace(exercisesAfterSplit: Vector[String], renumOffset: Int, renumStep: Int): Boolean =
    renumOffset + (exercisesAfterSplit.size - 1) * renumStep > 999
  end exceedsAvailableSpace

  private def rangeOverlapsWithOtherExercises(before: Vector[Int], renumOffset: Int): Boolean =
    before.nonEmpty && (renumOffset <= before.last)
  end rangeOverlapsWithOtherExercises

  def duplicateInsertBefore(mainRepo: File, exerciseNumber: Int)(config: CMTaConfig): Either[String, Unit] =
    val ExercisePrefixesAndExerciseNames(prefixes, exercises) =
      getExercisePrefixAndExercises(mainRepo)(config)
    validatePrefixes(prefixes)
    val exercisePrefix = prefixes.head

    val exerciseNumbers = exercises.map(extractExerciseNr)
    val mainRepoExerciseFolder = mainRepo / config.mainRepoExerciseFolder

    if !exerciseNumbers.contains(exerciseNumber) then Left(s"No exercise with number $exerciseNumber")
    else
      val splitIndex = exerciseNumbers.indexOf(exerciseNumber)
      val (exercisesNumsBeforeInsert, exercisesNumsAfterInsert) = exerciseNumbers.splitAt(splitIndex)
      val (exercisesBeforeInsert, exercisesAfterInsert) = exercises.splitAt(splitIndex)
      if exerciseNumber + exercisesNumsAfterInsert.size <= 999 then
        if exerciseNumber == 0 || exercisesNumsBeforeInsert.nonEmpty && exercisesNumsBeforeInsert.last == exerciseNumber - 1
        then
          renumberExercises(mainRepo, Some(exerciseNumber), exerciseNumber + 1, 1)(config)
          val duplicateFrom =
            mainRepoExerciseFolder / renumberExercise(exercisesAfterInsert.head, exercisePrefix, exerciseNumber + 1)
          val duplicateTo = mainRepoExerciseFolder / s"${exercisesAfterInsert.head}_copy"
          sbtio.copyDirectory(duplicateFrom, duplicateTo)
          Right(())
        else
          val duplicateFrom = mainRepoExerciseFolder / exercisesAfterInsert.head
          val duplicateTo =
            mainRepoExerciseFolder / s"${renumberExercise(exercisesAfterInsert.head, exercisePrefix, exerciseNumber - 1)}_copy"
          sbtio.copyDirectory(duplicateFrom, duplicateTo)
          Right(())
      else Left("Cannot duplicate and insert an exercise as it would exceed the available exercise number space")
  end duplicateInsertBefore

  private def renumberExercise(exercise: String, exercisePrefix: String, newNumber: Int): String =
    val newNumberPrefix = f"${exercisePrefix}_$newNumber%03d_"
    val oldNumberPrefix =
      f"${exercisePrefix}_${extractExerciseNr(exercise)}%03d_"
    exercise.replaceFirst(oldNumberPrefix, newNumberPrefix)

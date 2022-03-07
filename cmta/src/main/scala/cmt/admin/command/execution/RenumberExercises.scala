package cmt.admin.command.execution

import cmt.Helpers.{ExercisesMetadata, extractExerciseNr, getExerciseMetadata, validatePrefixes}
import cmt.admin.command.AdminCommand.RenumberExercises
import cmt.admin.command.execution.renumberExercise
import cmt.core.execution.Executable
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[RenumberExercises] with
  import RenumberExercisesHelpers.*

  extension (cmd: RenumberExercises)
    def execute(): Either[String, String] =
      for {
        ExercisesMetadata(exercisePrefix, exercises, exerciseNumbers) <- getExerciseMetadata(cmd.mainRepository.value)(
          cmd.config)

        mainRepoExerciseFolder = cmd.mainRepository.value / cmd.config.mainRepoExerciseFolder

        renumStartAt <- resolveStartAt(cmd.maybeStart.map(_.value), exerciseNumbers)

        splitIndex = exerciseNumbers.indexOf(renumStartAt)
        (exerciseNumsBeforeSplit, exerciseNumsAfterSplit) = exerciseNumbers.splitAt(splitIndex)
        (_, exercisesAfterSplit) = exercises.splitAt(splitIndex)

        renumOffset = cmd.offset.value
        tryMove = (exerciseNumsBeforeSplit, exerciseNumsAfterSplit) match
          case (Vector(), Vector(`renumOffset`, _)) =>
            Left("Renumber: nothing to renumber")
          case (before, _) if rangeOverlapsWithOtherExercises(before, renumOffset) =>
            Left("Moved exercise range overlaps with other exercises")
          case (_, _)
              if exceedsAvailableSpace(exercisesAfterSplit, renumOffset = renumOffset, renumStep = cmd.step.value) =>
            Left(s"Cannot renumber exercises as it would exceed the available exercise number space")
          case (_, _) =>
            val moves =
              for {
                (exercise, index) <- exercisesAfterSplit.zipWithIndex
                newNumber = renumOffset + index * cmd.step.value
                oldExerciseFolder = mainRepoExerciseFolder / exercise
                newExerciseFolder =
                  mainRepoExerciseFolder / renumberExercise(exercise, exercisePrefix, newNumber)
                if oldExerciseFolder != newExerciseFolder
              } yield (oldExerciseFolder, newExerciseFolder)

            if moves.isEmpty
            then Left("Renumber: nothing to renumber")
            else
              if renumOffset > renumStartAt
              then sbtio.move(moves.reverse)
              else sbtio.move(moves)
              Right(
                s"Renumbered exercises in ${cmd.mainRepository.value.getPath} from ${cmd.maybeStart} to ${cmd.offset.value} by ${cmd.step.value}")

        moveResult <- tryMove
      } yield moveResult
end given

private object RenumberExercisesHelpers:
  def resolveStartAt(renumStartAtOpt: Option[Int], exerciseNumbers: Vector[Int]) = {
    renumStartAtOpt match
      case None => Right(exerciseNumbers.head)
      case Some(num) =>
        if exerciseNumbers.contains(num)
        then Right(num)
        else Left(s"No exercise with number $num")
  }
  end resolveStartAt

  def exceedsAvailableSpace(exercisesAfterSplit: Vector[String], renumOffset: Int, renumStep: Int): Boolean =
    renumOffset + (exercisesAfterSplit.size - 1) * renumStep > 999
  end exceedsAvailableSpace

  def rangeOverlapsWithOtherExercises(before: Vector[Int], renumOffset: Int): Boolean =
    before.nonEmpty && (renumOffset <= before.last)
  end rangeOverlapsWithOtherExercises
end RenumberExercisesHelpers

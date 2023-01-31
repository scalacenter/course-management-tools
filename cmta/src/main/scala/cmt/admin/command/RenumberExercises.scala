package cmt.admin.command

import caseapp.{AppName, Command, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs, ValueDescription}
import cmt.{CMTaConfig, CmtError, printResult}
import cmt.Helpers.{
  ExercisesMetadata,
  commitToGit,
  exitIfGitIndexOrWorkspaceIsntClean,
  extractExerciseNr,
  getExerciseMetadata,
  validatePrefixes
}
import cmt.admin.Domain.{RenumberOffset, RenumberStart, RenumberStep}
import cmt.admin.cli.SharedOptions
import cmt.core.execution.Executable
import cmt.admin.cli.ArgParsers.{renumberOffsetArgParser, renumberStartArgParser, renumberStepArgParser}
import cmt.admin.command.RenumberExercises
import cmt.core.cli.CmtCommand
import cmt.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.toExecuteCommandErrorMessage

object RenumberExercises:

  def successMessage(options: Options): String =
    val fromAsString = if (options.from.isEmpty) "" else s" from ${options.from.get.value}"
    s"Renumbered exercises in ${options.shared.mainRepository.value.getPath}${fromAsString} to ${options.to.value} by ${options.step.value}"

  @AppName("renumber-exercises")
  @CommandName("renumber-exercises")
  @HelpMessage("Renumbers the exercises in the main repository")
  final case class Options(
      @ExtraName("f")
      @ValueDescription("Renumbering starting position.")
      @HelpMessage("The sequence number of the first exercise in the series to be renumbered")
      from: Option[RenumberStart] = None,
      @ExtraName("t")
      @ValueDescription("Renumbering destination position.")
      @HelpMessage("The new sequence number of the first exercise in the renumbering process")
      to: RenumberOffset = RenumberOffset(1),
      @ExtraName("s")
      @ValueDescription("Renumbering step size.")
      @HelpMessage("Renumbered exercises will be separated by this value")
      step: RenumberStep = RenumberStep(1),
      @Recurse shared: SharedOptions)

  given Validatable[RenumberExercises.Options] with
    extension (options: RenumberExercises.Options)
      def validated(): Either[CmtError, RenumberExercises.Options] =
        Right(options)
  end given

  given Executable[RenumberExercises.Options] with
    extension (options: RenumberExercises.Options)
      def execute(): Either[CmtError, String] = {
        import RenumberExercisesHelpers.*

        val mainRepository = options.shared.mainRepository
        val config = new CMTaConfig(mainRepository.value, options.shared.maybeConfigFile.map(_.value))

        for {
          _ <- exitIfGitIndexOrWorkspaceIsntClean(mainRepository.value)

          ExercisesMetadata(exercisePrefix, exercises, exerciseNumbers) <- getExerciseMetadata(mainRepository.value)(
            config)

          mainRepoExerciseFolder = mainRepository.value / config.mainRepoExerciseFolder

          renumberStartAt <- resolveStartAt(options.from.map(_.value), exerciseNumbers)

          splitIndex = exerciseNumbers.indexOf(renumberStartAt)
          (exerciseNumsBeforeSplit, exerciseNumsAfterSplit) = exerciseNumbers.splitAt(splitIndex)
          (_, exercisesAfterSplit) = exercises.splitAt(splitIndex)

          renumberOffset = options.to.value
          tryMove = (exerciseNumsBeforeSplit, exerciseNumsAfterSplit) match
            case (Vector(), Vector(`renumberOffset`, _)) =>
              Left("Renumber: nothing to renumber".toExecuteCommandErrorMessage)
            case (before, _) if rangeOverlapsWithOtherExercises(before, renumberOffset) =>
              Left("Moved exercise range overlaps with other exercises".toExecuteCommandErrorMessage)
            case (_, _)
                if exceedsAvailableSpace(
                  exercisesAfterSplit,
                  renumOffset = renumberOffset,
                  renumStep = options.step.value) =>
              Left(
                s"Cannot renumber exercises as it would exceed the available exercise number space".toExecuteCommandErrorMessage)
            case (_, _) =>
              val moves =
                for {
                  (exercise, index) <- exercisesAfterSplit.zipWithIndex
                  newNumber = renumberOffset + index * options.step.value
                  oldExerciseFolder = mainRepoExerciseFolder / exercise
                  newExerciseFolder =
                    mainRepoExerciseFolder / renumberExercise(exercise, exercisePrefix, newNumber)
                  if oldExerciseFolder != newExerciseFolder
                } yield (oldExerciseFolder, newExerciseFolder)

              if moves.isEmpty
              then Left("Renumber: nothing to renumber".toExecuteCommandErrorMessage)
              else
                if renumberOffset > renumberStartAt
                then sbtio.move(moves.reverse)
                else sbtio.move(moves)
                Right(successMessage(options))

          moveResult <- tryMove
          _ <- commitToGit(
            s"Checkpoint result of running 'ctma renumber-exercises -f $renumberStartAt -t $renumberOffset -s ${options.step.value}'",
            mainRepository.value)
        } yield moveResult
      }
  end given

  private object RenumberExercisesHelpers:
    def resolveStartAt(renumStartAtOpt: Option[Int], exerciseNumbers: Vector[Int]): Either[CmtError, Int] = {
      renumStartAtOpt match
        case None => Right(exerciseNumbers.head)
        case Some(num) =>
          if exerciseNumbers.contains(num)
          then Right(num)
          else Left(s"No exercise with number $num".toExecuteCommandErrorMessage)
    }
    end resolveStartAt

    def exceedsAvailableSpace(exercisesAfterSplit: Vector[String], renumOffset: Int, renumStep: Int): Boolean =
      renumOffset + (exercisesAfterSplit.size - 1) * renumStep > 999
    end exceedsAvailableSpace

    def rangeOverlapsWithOtherExercises(before: Vector[Int], renumOffset: Int): Boolean =
      before.nonEmpty && (renumOffset <= before.last)
    end rangeOverlapsWithOtherExercises
  end RenumberExercisesHelpers

  val command = new CmtCommand[RenumberExercises.Options] {
    def run(options: RenumberExercises.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end RenumberExercises

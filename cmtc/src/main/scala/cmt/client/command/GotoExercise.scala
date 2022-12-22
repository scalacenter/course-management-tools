package cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import cmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise}
import cmt.client.cli.SharedOptions
import cmt.client.command.ClientCommand.GotoExercise
import cmt.client.command.execution.getCurrentExerciseId
import cmt.core.CmtCommand
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import sbt.io.syntax.*
import cmt.client.cli.ArgParsers.{exerciseIdArgParser, forceMoveToExerciseArgParser}

object GotoExercise:

  @AppName("goto-exercise")
  @CommandName("goto-exercise")
  final case class Options(
      @ExtraName("e")
      exercise: ExerciseId,
      @ExtraName("f")
      force: ForceMoveToExercise = ForceMoveToExercise(false),
      @Recurse shared: SharedOptions)

  given Validatable[GotoExercise.Options] with
    extension (options: GotoExercise.Options)
      def validated(): Either[CmtError, GotoExercise.Options] =
        Right(options)
      end validated
  end given

  given Executable[GotoExercise.Options] with
    extension (options: GotoExercise.Options)
      def execute(): Either[CmtError, String] = {
        val config = new CMTcConfig(options.shared.studentifiedRepo.value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)

        val activeExerciseFolder = config.activeExerciseFolder
        val toExerciseId = options.exercise.value.toString

        if (!config.exercises.contains(toExerciseId))
          Left(toConsoleGreen(s"No such exercise: ${options.exercise.value}").toExecuteCommandErrorMessage)
        else
          val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
            getFilesToCopyAndDelete(currentExerciseId, toExerciseId, config)

          (options.force, currentExerciseId) match {
            case (_, `toExerciseId`) =>
              Left(s"You're already at exercise ${toExerciseId.toString}".toExecuteCommandErrorMessage)

            case (ForceMoveToExercise(true), _) =>
              pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, config)

            case _ =>
              val existingTestCodeFiles =
                currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

              val modifiedTestCodeFiles = existingTestCodeFiles.filter(
                exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, config))

              if (modifiedTestCodeFiles.nonEmpty)
                Left(s"""goto-exercise cancelled.
                        |
                        |${toConsoleYellow("You have modified the following file(s):")}
                        |${toConsoleGreen(modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n"))}
                        |""".stripMargin.toExecuteCommandErrorMessage)
              else
                pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, config)
          }
      }

  val command = new CmtCommand[GotoExercise.Options] {

    def run(options: GotoExercise.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end GotoExercise

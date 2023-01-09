package cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import cmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise}
import cmt.client.cli.SharedOptions
import cmt.client.command.getCurrentExerciseId
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import sbt.io.syntax.*
import cmt.client.cli.ArgParsers.{exerciseIdArgParser, forceMoveToExerciseArgParser}
import cmt.core.CmtCommand
import cmt.core.enforceTrailingArgumentCount

object GotoExercise:

  @AppName("goto-exercise")
  @CommandName("goto-exercise")
  @HelpMessage("Move to a given exercise. Pull in tests and readme files for that exercise")
  final case class Options(
      @ExtraName("e")
      exercise: Option[ExerciseId] = None,
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

        options.exercise
          .map { exercise =>
            val toExerciseId = exercise.value

            if (!config.exercises.contains(toExerciseId))
              Left(toConsoleGreen(s"No such exercise: ${toExerciseId}").toExecuteCommandErrorMessage)
            else
              val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
                getFilesToCopyAndDelete(currentExerciseId, toExerciseId, config)

              (options.force, currentExerciseId) match {
                case (_, `toExerciseId`) =>
                  Right(s"${toConsoleYellow("WARNING:")} ${toConsoleGreen(
                      s"You're already at exercise ${toConsoleYellow(toExerciseId)}")}")

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
          .getOrElse(Left("Exercise ID not specified".toExecuteCommandErrorMessage))
      }

  val command = new CmtCommand[GotoExercise.Options] {

    def run(options: GotoExercise.Options, args: RemainingArgs): Unit =
      args
        .enforceTrailingArgumentCount(expectedCount = 1)
        .flatMap(
          _.remaining.headOption
            .map(exerciseId => options.copy(exercise = Some(ExerciseId(exerciseId))))
            .getOrElse(options)
            .validated()
            .flatMap(_.execute()))
        .printResult()
  }

end GotoExercise

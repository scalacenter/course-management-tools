package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import coursemgmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.client.Configuration
import coursemgmt.client.Domain.{ExerciseId, ForceMoveToExercise}
import coursemgmt.core.validation.Validatable
import sbt.io.syntax.*
import coursemgmt.client.cli.ArgParsers.{exerciseIdArgParser, forceMoveToExerciseArgParser}
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.cli.enforceTrailingArgumentCount
import coursemgmt.core.command.Package.getCurrentExerciseId

object GotoExercise:

  @AppName("goto-exercise")
  @CommandName("goto-exercise")
  @HelpMessage("Move to a given exercise. Pull in tests and readme files for that exercise")
  final case class Options(
      @ExtraName("e")
      exercise: Option[ExerciseId] = None,
      @ExtraName("f")
      force: ForceMoveToExercise = ForceMoveToExercise(false),
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[GotoExercise.Options] with
    extension (options: GotoExercise.Options)
      def validated(): Either[CmtError, GotoExercise.Options] =
        Right(options)
      end validated
  end given

  given Executable[GotoExercise.Options] with
    extension (options: GotoExercise.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val config = new CMTcConfig(options.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
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
                    exerciseFileHasBeenModified(activeExerciseFolder, _, config.testCodeMetaData(currentExerciseId)))

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

  val command = new CmtcCommand[GotoExercise.Options] {

    def run(options: GotoExercise.Options, args: RemainingArgs): Unit =
      args
        .enforceTrailingArgumentCount(expectedCount = 1)
        .flatMap(
          _.remaining.headOption
            .map(exerciseId => options.copy(exercise = Some(ExerciseId(exerciseId))))
            .getOrElse(options)
            .validated()
            .flatMap(_.execute(configuration)))
        .printResult()
  }

end GotoExercise

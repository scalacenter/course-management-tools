package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import coursemgmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import coursemgmt.client.Configuration
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.client.Domain.ForceMoveToExercise
import coursemgmt.core.validation.Validatable
import sbt.io.syntax.*
import coursemgmt.client.cli.ArgParsers.forceMoveToExerciseArgParser
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.cli.enforceNoTrailingArguments
import coursemgmt.core.command.Package.getCurrentExerciseId

object PreviousExercise:

  @AppName("previous-exercise")
  @CommandName("previous-exercise")
  @HelpMessage("Move to the previous exercise. Pull in tests and readme files for that exercise")
  final case class Options(
      @ExtraName("f")
      force: ForceMoveToExercise = ForceMoveToExercise(false),
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[PreviousExercise.Options] with
    extension (options: PreviousExercise.Options)
      def validated(): Either[CmtError, PreviousExercise.Options] =
        Right(options)
      end validated
  end given

  extension (options: PreviousExercise.Options)
    def execute(configuration: Configuration): Either[CmtError, String] = {
      val studentifiedRepo = options.studentifiedRepo.getOrElse(configuration.currentCourse.value)
      val cMTcConfig = new CMTcConfig(studentifiedRepo.value)
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val FirstExerciseId = cMTcConfig.exercises.head

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cMTcConfig.previousExercise(currentExerciseId)

      val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
        getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

      (currentExerciseId, options.force) match {
        case (FirstExerciseId, _) =>
          Right(s"${toConsoleYellow("WARNING:")} ${toConsoleGreen(
              s"You're already at the first exercise: ${toConsoleYellow(currentExerciseId)}")}")

        case (_, ForceMoveToExercise(true)) =>
          pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

        case _ =>
          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, _, cMTcConfig.testCodeMetaData(currentExerciseId)))

          if (modifiedTestCodeFiles.nonEmpty)
            Left(s"""previous-exercise cancelled.
                    |
                    |${toConsoleYellow("You have modified the following file(s):")}
                    |${toConsoleGreen(modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n"))}
                    |""".stripMargin.toExecuteCommandErrorMessage)
          else
            pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)
      }
    }

  val command = new CmtcCommand[PreviousExercise.Options] {

    def run(options: PreviousExercise.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end PreviousExercise

package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import coursemgmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.client.Configuration
import coursemgmt.client.Domain.ForceMoveToExercise
import coursemgmt.core.validation.Validatable
import sbt.io.syntax.*
import coursemgmt.client.cli.ArgParsers.forceMoveToExerciseArgParser
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.cli.enforceNoTrailingArguments
import coursemgmt.core.command.Package.getCurrentExerciseId

object NextExercise:

  @AppName("next-exercise")
  @CommandName("next-exercise")
  @HelpMessage("Move to the next exercise. Pull in tests and readme files for that exercise")
  final case class Options(
      @ExtraName("f")
      force: ForceMoveToExercise = ForceMoveToExercise(false),
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[NextExercise.Options] with
    extension (options: NextExercise.Options)
      def validated(): Either[CmtError, NextExercise.Options] =
        Right(options)
      end validated
  end given

  extension (options: NextExercise.Options)
    def execute(configuration: Configuration): Either[CmtError, String] = {
      val cMTcConfig = new CMTcConfig(options.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)

      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val LastExerciseId = cMTcConfig.exercises.last

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cMTcConfig.nextExercise(currentExerciseId)

      val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
        getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

      (currentExerciseId, options.force) match {
        case (LastExerciseId, _) =>
          Right(s"${toConsoleYellow("WARNING:")} ${toConsoleGreen(
              s"You're already at the last exercise: ${toConsoleYellow(currentExerciseId)}")}")

        case (_, ForceMoveToExercise(true)) =>
          pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

        case _ =>
          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, _, cMTcConfig.testCodeMetaData(currentExerciseId)))

          if (modifiedTestCodeFiles.nonEmpty)
            // TODO: need to add a suggested fix when this case triggers:
            // Either:
            //   - overwrite modifications by repeating the command and using the force (-f) option
            //     maybe in combination with cmtc save-state in case the modifications should be
            //     retrieveable later
            //   - rename modified files (and probably change class names as well)
            //
            // This needs to be added to the `previous-exercise`, `goto-exercise`, and `goto-first-exercise`
            // commands too.
            Left(s"""next-exercise cancelled.
                    |
                    |${toConsoleYellow("You have modified the following file(s):")}
                    |${toConsoleGreen(modifiedTestCodeFiles.mkString("\n   ", "\n   ", "\n"))}
                    |""".stripMargin.toExecuteCommandErrorMessage)
          else
            pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)
      }
    }

  val command = new CmtcCommand[NextExercise.Options] {

    def run(options: NextExercise.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }
end NextExercise

package cmt.client.command

import caseapp.{AppName, CommandName, HelpMessage, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import cmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import cmt.client.Domain.ForceMoveToExercise
import cmt.client.cli.SharedOptions
import cmt.client.command.getCurrentExerciseId
import cmt.core.CmtCommand
import cmt.core.validation.Validatable
import sbt.io.syntax.*
import cmt.client.cli.ArgParsers.forceMoveToExerciseArgParser

object PreviousExercise:

  @AppName("previous-exercise")
  @CommandName("previous-exercise")
  @HelpMessage("Move to the previous exercise. Pull in tests and readme files for that exercise")
  final case class Options(force: ForceMoveToExercise = ForceMoveToExercise(false), @Recurse shared: SharedOptions)

  given Validatable[PreviousExercise.Options] with
    extension (options: PreviousExercise.Options)
      def validated(): Either[CmtError, PreviousExercise.Options] =
        Right(options)
      end validated
  end given

  extension (cmd: PreviousExercise.Options)
    def execute(): Either[CmtError, String] = {
      val cMTcConfig = new CMTcConfig(cmd.shared.studentifiedRepo.value)
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val FirstExerciseId = cMTcConfig.exercises.head

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cMTcConfig.previousExercise(currentExerciseId)

      val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
        getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

      (currentExerciseId, cmd.force) match {
        case (FirstExerciseId, _) =>
          Right(s"${toConsoleYellow("WARNING:")} ${toConsoleGreen(
              s"You're already at the first exercise: ${toConsoleYellow(currentExerciseId)}")}")

        case (_, ForceMoveToExercise(true)) =>
          pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

        case _ =>
          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, cMTcConfig))

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

  val command = new CmtCommand[PreviousExercise.Options] {

    def run(options: PreviousExercise.Options, args: RemainingArgs): Unit = {
      enforceNoTrailingArguments(args)
      options.validated().flatMap(_.execute()).printResult()
    }
  }

end PreviousExercise

package cmt.client.command

import caseapp.{AppName, CommandName, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow, toExecuteCommandErrorMessage}
import cmt.Helpers.{exerciseFileHasBeenModified, getFilesToCopyAndDelete, pullTestCode}
import cmt.client.Domain.ForceMoveToExercise
import cmt.client.cli.SharedOptions
import cmt.client.command.ClientCommand.NextExercise
import cmt.client.command.execution.getCurrentExerciseId
import cmt.core.CmtCommand
import cmt.core.validation.Validatable
import sbt.io.syntax.*
import cmt.client.cli.ArgParsers.forceMoveToExerciseArgParser

object NextExercise:

  @AppName("next-exercise")
  @CommandName("next-exercise")
  final case class Options(force: ForceMoveToExercise = ForceMoveToExercise(false), @Recurse shared: SharedOptions)

  given Validatable[NextExercise.Options] with
    extension (options: NextExercise.Options)
      def validated(): Either[CmtError, NextExercise.Options] =
        Right(options)
      end validated
  end given

  extension (cmd: NextExercise.Options)
    def execute(): Either[CmtError, String] = {
      import cmt.client.Domain.ForceMoveToExercise
      val cMTcConfig = new CMTcConfig(cmd.shared.studentifiedRepo.value)
      val currentExerciseId = getCurrentExerciseId(cMTcConfig.bookmarkFile)
      val LastExerciseId = cMTcConfig.exercises.last

      val activeExerciseFolder = cMTcConfig.activeExerciseFolder
      val toExerciseId = cMTcConfig.nextExercise(currentExerciseId)

      val (currentTestCodeFiles, filesToBeDeleted, filesToBeCopied) =
        getFilesToCopyAndDelete(currentExerciseId, toExerciseId, cMTcConfig)

      (currentExerciseId, cmd.force) match {
        case (LastExerciseId, _) =>
          Left(toConsoleGreen(s"You're already at the last exercise: $currentExerciseId").toExecuteCommandErrorMessage)

        case (_, ForceMoveToExercise(true)) =>
          pullTestCode(toExerciseId, activeExerciseFolder, filesToBeDeleted, filesToBeCopied, cMTcConfig)

        case _ =>
          val existingTestCodeFiles =
            currentTestCodeFiles.filter(file => (activeExerciseFolder / file).exists())

          val modifiedTestCodeFiles = existingTestCodeFiles.filter(
            exerciseFileHasBeenModified(activeExerciseFolder, currentExerciseId, _, cMTcConfig))

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

  val command = new CmtCommand[NextExercise.Options] {

    def run(options: NextExercise.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }
end NextExercise

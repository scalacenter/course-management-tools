package cmt.client.command

import caseapp.{AppName, CommandName, Recurse, RemainingArgs}
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen}
import cmt.client.cli.SharedOptions
import cmt.client.command.ClientCommand.ListExercises
import cmt.client.command.execution.{getCurrentExerciseId, starCurrentExercise}
import cmt.core.CmtCommand
import cmt.core.execution.Executable
import cmt.core.validation.Validatable

object ListExercises:

  @AppName("list-exercises")
  @CommandName("list-exercises")
  final case class Options(@Recurse shared: SharedOptions)

  given Validatable[ListExercises.Options] with
    extension (options: ListExercises.Options)
      def validated(): Either[CmtError, ListExercises.Options] =
        Right(options)
      end validated
  end given

  given Executable[ListExercises.Options] with
    extension (cmd: ListExercises.Options)
      def execute(): Either[CmtError, String] = {
        val config = new CMTcConfig(cmd.shared.studentifiedRepo.value)
        val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)

        val messages = config.exercises.zipWithIndex
          .map { case (exName, index) =>
            toConsoleGreen(f"${index + 1}%3d. ${starCurrentExercise(currentExerciseId, exName)}  $exName")
          }
          .mkString("\n")
        Right(messages)
      }
    end extension
  end given

  val command = new CmtCommand[ListExercises.Options] {

    def run(options: ListExercises.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end ListExercises

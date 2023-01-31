package cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import cmt.client.Configuration
import cmt.client.Domain.StudentifiedRepo
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen}
import cmt.client.command.{getCurrentExerciseId, starCurrentExercise}
import cmt.client.cli.CmtcCommand
import cmt.core.validation.Validatable
import cmt.core.cli.enforceNoTrailingArguments
import cmt.client.cli.ArgParsers.studentifiedRepoArgParser

object ListExercises:

  @AppName("list-exercises")
  @CommandName("list-exercises")
  @HelpMessage("List all exercises and their IDs in the repo. Mark the active exercise with a star")
  final case class Options(
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[ListExercises.Options] with
    extension (options: ListExercises.Options)
      def validated(): Either[CmtError, ListExercises.Options] =
        Right(options)
      end validated
  end given

  given Executable[ListExercises.Options] with
    extension (options: ListExercises.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val config = new CMTcConfig(options.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
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

  val command = new CmtcCommand[ListExercises.Options] {

    def run(options: ListExercises.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end ListExercises

package coursemgmttools.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmttools.client.Configuration
import coursemgmttools.Domain.StudentifiedRepo
import coursemgmttools.{CMTcConfig, CmtError, printResult}
import coursemgmttools.client.cli.CmtcCommand
import coursemgmttools.core.validation.Validatable
import coursemgmttools.core.cli.enforceNoTrailingArguments
import coursemgmttools.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmttools.Helpers.listExercises

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
        Right(listExercises(config))
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

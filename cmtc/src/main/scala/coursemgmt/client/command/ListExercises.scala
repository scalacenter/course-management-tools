package coursemgmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmt.client.Configuration
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.{CMTcConfig, CmtError, printResult}
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.validation.Validatable
import coursemgmt.core.cli.enforceNoTrailingArguments
import coursemgmt.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmt.Helpers.listExercises

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

package coursemgmttools.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import coursemgmttools.client.Configuration
import coursemgmttools.{CMTcConfig, CmtError, printResult}
import coursemgmttools.client.Domain.{ExerciseId, ForceMoveToExercise}
import coursemgmttools.Domain.StudentifiedRepo
import coursemgmttools.core.validation.Validatable
import coursemgmttools.client.cli.ArgParsers.forceMoveToExerciseArgParser
import coursemgmttools.core.cli.ArgParsers.studentifiedRepoArgParser
import coursemgmttools.client.cli.CmtcCommand
import coursemgmttools.core.cli.enforceNoTrailingArguments

object GotoFirstExercise:

  @AppName("goto-first-exercise")
  @CommandName("goto-first-exercise")
  @HelpMessage("Move to the first exercise. Pull in tests and readme files for that exercise")
  final case class Options(
      @ExtraName("f")
      force: ForceMoveToExercise = ForceMoveToExercise(false),
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[GotoFirstExercise.Options] with
    extension (options: GotoFirstExercise.Options)
      def validated(): Either[CmtError, GotoFirstExercise.Options] =
        Right(options)
      end validated
  end given

  given Executable[GotoFirstExercise.Options] with
    extension (options: GotoFirstExercise.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val config = new CMTcConfig(options.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
        GotoExercise
          .Options(
            exercise = Some(ExerciseId(config.exercises.head)),
            force = options.force,
            studentifiedRepo = options.studentifiedRepo)
          .execute(configuration)
      }

  val command = new CmtcCommand[GotoFirstExercise.Options] {

    def run(options: GotoFirstExercise.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end GotoFirstExercise

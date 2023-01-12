package cmt.client.command

import caseapp.{AppName, CommandName, HelpMessage, Recurse, RemainingArgs}
import cmt.client.Configuration
import cmt.{CMTcConfig, CmtError, printResult}
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise, StudentifiedRepo}
import cmt.client.cli.SharedOptions
import cmt.client.command.GotoExercise
import cmt.client.command.Executable
import cmt.core.validation.Validatable
import cmt.client.cli.ArgParsers.{forceMoveToExerciseArgParser, studentifiedRepoArgParser}
import cmt.client.cli.CmtcCommand
import cmt.core.cli.enforceNoTrailingArguments

object GotoFirstExercise:

  @AppName("goto-first-exercise")
  @CommandName("goto-first-exercise")
  @HelpMessage("Move to the first exercise. Pull in tests and readme files for that exercise")
  final case class Options(force: ForceMoveToExercise = ForceMoveToExercise(false), @Recurse shared: SharedOptions)

  given Validatable[GotoFirstExercise.Options] with
    extension (options: GotoFirstExercise.Options)
      def validated(): Either[CmtError, GotoFirstExercise.Options] =
        Right(options)
      end validated
  end given

  given Executable[GotoFirstExercise.Options] with
    extension (options: GotoFirstExercise.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val config = new CMTcConfig(options.shared.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
        GotoExercise
          .Options(exercise = Some(ExerciseId(config.exercises.head)), force = options.force, shared = options.shared)
          .execute(configuration)
      }

  val command = new CmtcCommand[GotoFirstExercise.Options] {

    def run(options: GotoFirstExercise.Options, args: RemainingArgs): Unit =
      args.enforceNoTrailingArguments().flatMap(_ => options.validated().flatMap(_.execute(configuration))).printResult()
  }

end GotoFirstExercise

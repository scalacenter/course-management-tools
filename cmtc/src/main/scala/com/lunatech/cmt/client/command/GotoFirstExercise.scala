package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.{CMTcConfig, CmtError, printResult}
import com.lunatech.cmt.client.Domain.{ExerciseId, ForceMoveToExercise}
import com.lunatech.cmt.Domain.StudentifiedRepo
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.client.cli.ArgParsers.{forceMoveToExerciseArgParser, studentifiedRepoArgParser}
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments

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

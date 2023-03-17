package com.lunatech.cmt.client.command

import com.lunatech.cmt.client.Domain.{ExerciseId, StudentifiedRepo}
import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import com.lunatech.cmt.client.cli.ArgParsers.{exerciseIdArgParser, studentifiedRepoArgParser}
import com.lunatech.cmt.client.command.deleteCurrentState
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.*
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.client.cli.CmtcCommand
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.lunatech.cmt.core.cli.enforceTrailingArgumentCount

object RestoreState:

  @AppName("restore-state")
  @CommandName("restore-state")
  @HelpMessage("Restore a previously saved exercise state")
  final case class Options(
      @ExtraName("e")
      exercise: Option[ExerciseId] = None,
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[RestoreState.Options] with
    extension (options: RestoreState.Options)
      def validated(): Either[CmtError, RestoreState.Options] =
        Right(options)
      end validated
  end given

  given Executable[RestoreState.Options] with
    extension (options: RestoreState.Options)
      def execute(configuration: Configuration): Either[CmtError, String] = {
        val studentifiedRepo = options.studentifiedRepo.getOrElse(configuration.currentCourse.value)
        val config = new CMTcConfig(studentifiedRepo.value)

        options.exercise
          .map { exercise =>
            val savedState = config.studentifiedSavedStatesFolder / s"${exercise.value}.zip"
            if !savedState.exists
            then Left(s"No such saved state: ${exercise.value}".toExecuteCommandErrorMessage)
            else {
              deleteCurrentState(studentifiedRepo.value)(config)

              Helpers.withZipFile(config.studentifiedSavedStatesFolder, exercise.value) { solution =>
                sbtio.copyDirectory(
                  config.studentifiedSavedStatesFolder / exercise.value,
                  config.activeExerciseFolder,
                  preserveLastModified = true)

                Helpers.writeStudentifiedCMTBookmark(config.bookmarkFile, exercise.value)
                Right(toConsoleGreen(s"Restored state for ${toConsoleYellow(exercise.value)}"))
              }
            }
          }
          .getOrElse(Left("Exercise ID not specified".toExecuteCommandErrorMessage))
      }

  val command = new CmtcCommand[RestoreState.Options] {

    def run(options: RestoreState.Options, args: RemainingArgs): Unit =
      args
        .enforceTrailingArgumentCount(expectedCount = 1)
        .flatMap(
          _.remaining.headOption
            .map(exercise => options.copy(exercise = Some(ExerciseId(exercise))))
            .getOrElse(options)
            .validated()
            .flatMap(_.execute(configuration)))
        .printResult()
  }
end RestoreState

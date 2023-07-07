package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import com.lunatech.cmt.Domain.StudentifiedRepo
import com.lunatech.cmt.client.cli.ArgParsers.studentifiedRepoArgParser
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.client.{Configuration, CurrentCourse, findStudentRepoRoot, listExercises}
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.{CMTcConfig, CmtError, printResult}
import sbt.io.syntax.*

object SetCurrentCourse:

  @AppName("set-current-course")
  @CommandName("set-current-course")
  @HelpMessage("Sets the current course to point to a directory")
  final case class Options(
      @ExtraName("s")
      directory: StudentifiedRepo)

  given Validatable[SetCurrentCourse.Options] with
    extension (options: SetCurrentCourse.Options)
      def validated(): Either[CmtError, SetCurrentCourse.Options] =
        Right(options)
      end validated
  end given

  given Executable[SetCurrentCourse.Options] with
    extension (options: SetCurrentCourse.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        for {
          studentRepoRoot <- findStudentRepoRoot(options.directory.value)
          formattedExerciseList = listExercises(new CMTcConfig(studentRepoRoot))
          currentCourse <- configuration
            .copy(currentCourse = CurrentCourse(StudentifiedRepo(studentRepoRoot)))
            .flush()
            .map(_ => s"""Current course set to '${studentRepoRoot.getAbsolutePath}'
                 |
                 |Exercises in repository:
                 |$formattedExerciseList""".stripMargin)
        } yield currentCourse

  val command = new CmtcCommand[SetCurrentCourse.Options] {

    def run(options: SetCurrentCourse.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end SetCurrentCourse

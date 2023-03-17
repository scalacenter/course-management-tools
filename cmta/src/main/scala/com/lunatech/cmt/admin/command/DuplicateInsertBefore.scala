package com.lunatech.cmt.admin.command

import com.lunatech.cmt.*
import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Recurse, RemainingArgs, ValueDescription}
import com.lunatech.cmt.Helpers.{
  ExercisesMetadata,
  getExerciseMetadata,
  commitToGit,
  exitIfGitIndexOrWorkspaceIsntClean
}
import com.lunatech.cmt.admin.Domain.{ExerciseNumber, RenumberOffset, RenumberStart, RenumberStep}
import com.lunatech.cmt.admin.cli.SharedOptions
import com.lunatech.cmt.core.execution.Executable
import com.lunatech.cmt.core.validation.Validatable
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.lunatech.cmt.admin.cli.ArgParsers.exerciseNumberArgParser
import com.lunatech.cmt.core.cli.CmtCommand

object DuplicateInsertBefore:

  @AppName("duplicate-insert-before")
  @CommandName("duplicate-insert-before")
  @HelpMessage("Duplicates a given exercise in a 'main' repository shifting subsequent exercises if needed")
  final case class Options(
      @ExtraName("n")
      @ValueDescription("Sequence number of the exercise to be duplicated")
      exerciseNumber: ExerciseNumber,
      @Recurse shared: SharedOptions)

  given Validatable[DuplicateInsertBefore.Options] with
    extension (options: DuplicateInsertBefore.Options)
      def validated(): Either[CmtError, DuplicateInsertBefore.Options] =
        Right(options)
  end given

  given Executable[DuplicateInsertBefore.Options] with
    extension (options: DuplicateInsertBefore.Options)
      def execute(): Either[CmtError, String] = {

        val mainRepository = options.shared.mainRepository
        val config = new CMTaConfig(mainRepository.value, options.shared.maybeConfigFile.map(_.value))

        for {
          _ <- exitIfGitIndexOrWorkspaceIsntClean(mainRepository.value)
          ExercisesMetadata(exercisePrefix, exercises, exerciseNumbers) <- getExerciseMetadata(mainRepository.value)(
            config)

          mainRepoExerciseFolder = mainRepository.value / config.mainRepoExerciseFolder

          duplicateInsertBeforeResult <-
            if !exerciseNumbers.contains(options.exerciseNumber.value)
            then Left(s"No exercise with number ${options.exerciseNumber.value}".toExecuteCommandErrorMessage)
            else
              val splitIndex = exerciseNumbers.indexOf(options.exerciseNumber.value)
              val (exercisesNumsBeforeInsert, exercisesNumsAfterInsert) = exerciseNumbers.splitAt(splitIndex)
              val (_, exercisesAfterInsert) = exercises.splitAt(splitIndex)
              if options.exerciseNumber.value + exercisesNumsAfterInsert.size <= 999 then
                if options.exerciseNumber.value == 0 || exercisesNumsBeforeInsert.nonEmpty && exercisesNumsBeforeInsert.last == options.exerciseNumber.value - 1
                then
                  RenumberExercises
                    .Options(
                      Some(RenumberStart(options.exerciseNumber.value)),
                      RenumberOffset(options.exerciseNumber.value + 1),
                      RenumberStep(1),
                      options.shared)
                    .execute()
                  val duplicateFrom =
                    mainRepoExerciseFolder / renumberExercise(
                      exercisesAfterInsert.head,
                      exercisePrefix,
                      options.exerciseNumber.value + 1)
                  val duplicateTo = mainRepoExerciseFolder / s"${exercisesAfterInsert.head}_copy"
                  sbtio.copyDirectory(duplicateFrom, duplicateTo)
                else
                  val duplicateFrom = mainRepoExerciseFolder / exercisesAfterInsert.head
                  val duplicateTo =
                    mainRepoExerciseFolder / s"${renumberExercise(exercisesAfterInsert.head, exercisePrefix, options.exerciseNumber.value - 1)}_copy"
                  sbtio.copyDirectory(duplicateFrom, duplicateTo)
                commitToGit(
                  s"Checkpoint result of running 'ctma duplicate-insert-before -n ${options.exerciseNumber.value}'",
                  mainRepository.value).flatMap(_ =>
                  Right(s"Duplicated and inserted exercise ${options.exerciseNumber.value}"))
              else
                Left(
                  "Cannot duplicate and insert an exercise as it would exceed the available exercise number space".toExecuteCommandErrorMessage)

        } yield duplicateInsertBeforeResult
      }
  end given

  val command = new CmtCommand[DuplicateInsertBefore.Options] {
    def run(options: DuplicateInsertBefore.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end DuplicateInsertBefore

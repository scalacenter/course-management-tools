package cmt.admin.command.execution

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import cmt.admin.command.execution.renumberExercise
import cmt.Helpers.{ExercisesMetadata, extractExerciseNr, getExerciseMetadata, validatePrefixes}
import cmt.admin.Domain.{RenumberOffset, RenumberStart, RenumberStep}
import cmt.admin.command.AdminCommand.{DuplicateInsertBefore, RenumberExercises}
import cmt.admin.command.execution.renumberExercise
import cmt.core.execution.Executable
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[DuplicateInsertBefore] with
  extension (cmd: DuplicateInsertBefore)
    def execute(): Either[String, String] = {
      for {
        ExercisesMetadata(exercisePrefix, exercises, exerciseNumbers) <- getExerciseMetadata(cmd.mainRepository.value)(
          cmd.config)

        mainRepoExerciseFolder = cmd.mainRepository.value / cmd.config.mainRepoExerciseFolder

        duplicateInsertBeforeResult <-
          if !exerciseNumbers.contains(cmd.exerciseNumber.value)
          then Left(s"No exercise with number ${cmd.exerciseNumber.value}")
          else
            val splitIndex = exerciseNumbers.indexOf(cmd.exerciseNumber.value)
            val (exercisesNumsBeforeInsert, exercisesNumsAfterInsert) = exerciseNumbers.splitAt(splitIndex)
            val (exercisesBeforeInsert, exercisesAfterInsert) = exercises.splitAt(splitIndex)
            if cmd.exerciseNumber.value + exercisesNumsAfterInsert.size <= 999 then
              if cmd.exerciseNumber.value == 0 || exercisesNumsBeforeInsert.nonEmpty && exercisesNumsBeforeInsert.last == cmd.exerciseNumber.value - 1
              then
                RenumberExercises(
                  cmd.mainRepository,
                  cmd.config,
                  Some(RenumberStart(cmd.exerciseNumber.value)),
                  RenumberOffset(cmd.exerciseNumber.value + 1),
                  RenumberStep(1)).execute()
                val duplicateFrom =
                  mainRepoExerciseFolder / renumberExercise(
                    exercisesAfterInsert.head,
                    exercisePrefix,
                    cmd.exerciseNumber.value + 1)
                val duplicateTo = mainRepoExerciseFolder / s"${exercisesAfterInsert.head}_copy"
                sbtio.copyDirectory(duplicateFrom, duplicateTo)
                Right(s"Duplicated and inserted exercise ${cmd.exerciseNumber.value}")
              else
                val duplicateFrom = mainRepoExerciseFolder / exercisesAfterInsert.head
                val duplicateTo =
                  mainRepoExerciseFolder / s"${renumberExercise(exercisesAfterInsert.head, exercisePrefix, cmd.exerciseNumber.value - 1)}_copy"
                sbtio.copyDirectory(duplicateFrom, duplicateTo)
                Right(s"Duplicated and inserted exercise ${cmd.exerciseNumber.value}")
            else Left("Cannot duplicate and insert an exercise as it would exceed the available exercise number space")

      } yield duplicateInsertBeforeResult
    }
end given

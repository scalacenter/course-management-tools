package cmt.client.cli

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

import caseapp.Parser
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise, StudentifiedRepo}
import cmt.client.command.GotoExercise
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import cmt.support.{CommandLineArguments, TestDirectories}
import sbt.io.syntax.File
import cmt.client.cli.ArgParsers.*

final class GotoExerciseArgumentsSpec extends CommandLineArgumentsSpec[GotoExercise.Options] with TestDirectories {

  val identifier = "goto-exercise"

  val parser: Parser[GotoExercise.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-s", baseDirectoryGitRoot.getAbsolutePath, "-e", "99"),
      GotoExercise.Options(
        exercise = Some(ExerciseId("99")),
        force = ForceMoveToExercise(false),
        studentifiedRepo = Some(StudentifiedRepo(baseDirectoryGitRoot)))),
    (
      Seq("-s", baseDirectoryGitRoot.getAbsolutePath, "99"),
      GotoExercise.Options(
        exercise = None,
        force = ForceMoveToExercise(false),
        studentifiedRepo = Some(StudentifiedRepo(baseDirectoryGitRoot)))))
}

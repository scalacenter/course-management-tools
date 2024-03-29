package coursemgmt.client.cli

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
import coursemgmt.Domain.StudentifiedRepo
import coursemgmt.client.Domain.{ExerciseId, ForceMoveToExercise}
import coursemgmt.client.cli.ArgParsers.given
import coursemgmt.client.command.GotoExercise
import coursemgmt.core.cli.ArgParsers.given
import coursemgmt.support.TestDirectories
import sbt.io.syntax.File

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

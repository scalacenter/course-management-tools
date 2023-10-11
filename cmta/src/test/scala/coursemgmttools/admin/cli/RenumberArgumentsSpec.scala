package coursemgmttools.admin.cli

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
import coursemgmttools.{ErrorMessage, FailedToValidateArgument, OptionName, RequiredOptionIsMissing}
import coursemgmttools.admin.Domain.{MainRepository, RenumberOffset, RenumberStart, RenumberStep}
import coursemgmttools.admin.command.RenumberExercises
import coursemgmttools.support.TestDirectories
import sbt.io.syntax.{File, file}
import coursemgmttools.admin.cli.ArgParsers.{renumberStepArgParser, renumberOffsetArgParser, renumberStartArgParser}

final class RenumberArgumentsSpec extends CommandLineArgumentsSpec[RenumberExercises.Options] with TestDirectories {

  val identifier = "renum"

  val parser: Parser[RenumberExercises.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = {
    val nonExistentFile = nonExistentDirectory(tempDirectory)
    invalidArgumentsTable(
      (Seq.empty, Set(RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
      (
        Seq("-m", nonExistentDirectory(tempDirectory)),
        Set(
          FailedToValidateArgument(
            OptionName("m"),
            List(
              ErrorMessage(s"$nonExistentFile does not exist"),
              ErrorMessage(s"$nonExistentFile is not a directory"),
              ErrorMessage(s"$nonExistentFile is not in a git repository"))),
          RequiredOptionIsMissing(OptionName("--main-repository, -m")))))
  }

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-m", firstRealDirectory),
      RenumberExercises.Options(
        from = None,
        to = RenumberOffset(1),
        step = RenumberStep(1),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "--from", "9"),
      RenumberExercises.Options(
        from = Some(RenumberStart(9)),
        to = RenumberOffset(1),
        step = RenumberStep(1),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "--to", "99"),
      RenumberExercises.Options(
        from = None,
        to = RenumberOffset(99),
        step = RenumberStep(1),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "--step", "999"),
      RenumberExercises.Options(
        from = None,
        to = RenumberOffset(1),
        step = RenumberStep(999),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "--from", "1", "--to", "2", "--step", "3"),
      RenumberExercises.Options(
        from = Some(RenumberStart(1)),
        to = RenumberOffset(2),
        step = RenumberStep(3),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))))
}

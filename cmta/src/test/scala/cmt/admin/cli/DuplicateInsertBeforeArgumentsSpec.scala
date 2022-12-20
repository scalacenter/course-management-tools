package cmt.admin.cli

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
import cmt.{ErrorMessage, FailedToValidateArgument, Helpers, OptionName, RequiredOptionIsMissing}
import cmt.admin.Domain.{
  ExerciseNumber,
  ForceDeleteDestinationDirectory,
  InitializeGitRepo,
  MainRepository,
  StudentifyBaseDirectory
}
import cmt.admin.command.{DuplicateInsertBefore, Studentify}
import cmt.support.{CommandLineArguments, TestDirectories}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError
import cmt.admin.cli.ArgParsers.*
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}

final class DuplicateInsertBeforeArgumentsSpec
    extends CommandLineArgumentsSpec[DuplicateInsertBefore.Options]
    with TestDirectories {

  val identifier = "dib"

  val parser: Parser[DuplicateInsertBefore.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (
      Seq.empty,
      Set(
        RequiredOptionIsMissing(OptionName("--exercise-number, -n")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", nonExistentDirectory(tempDirectory)),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(
            ErrorMessage(s"$tempDirectory/i/do/not/exist does not exist"),
            ErrorMessage(s"$tempDirectory/i/do/not/exist is not a directory"),
            ErrorMessage(s"$tempDirectory/i/do/not/exist is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--exercise-number, -n")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", realFile),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(ErrorMessage(s"$realFile is not a directory"), ErrorMessage(s"$realFile is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--exercise-number, -n")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", tempDirectory.getAbsolutePath),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(ErrorMessage(s"${tempDirectory.getAbsolutePath} is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--exercise-number, -n")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))))

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-m", firstRealDirectory, "--exercise-number", "1"),
      DuplicateInsertBefore.Options(
        exerciseNumber = ExerciseNumber(1),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))))
}

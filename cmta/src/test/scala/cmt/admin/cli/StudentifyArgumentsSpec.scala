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
import caseapp.core.Error.{CannotBeDisabled, Other, ParsingArgument, RequiredOptionNotSpecified, SeveralErrors}
import cmt.{ErrorMessage, FailedToValidateArgument, Helpers, OptionName, RequiredOptionIsMissing}
import cmt.admin.command.Studentify
import cmt.admin.cli.ArgParsers.*
import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, MainRepository, StudentifyBaseDirectory}
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError
import org.scalatest.matchers.should.Matchers

final class StudentifyArgumentsSpec extends CommandLineArgumentsSpec[Studentify.Options] with TestDirectories {

  val identifier = "studentify"

  val parser: Parser[Studentify.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (
      Seq.empty,
      Set(
        RequiredOptionIsMissing(OptionName("--studentify-base-directory, -s")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", nonExistentDirectory(tempDirectory), "-s", nonExistentDirectory(tempDirectory)),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(
            ErrorMessage(s"$tempDirectory/i/do/not/exist does not exist"),
            ErrorMessage(s"$tempDirectory/i/do/not/exist is not a directory"),
            ErrorMessage(s"$tempDirectory/i/do/not/exist is not in a git repository"))),
        FailedToValidateArgument(
          OptionName("s"),
          List(
            ErrorMessage(s"$tempDirectory/i/do/not/exist does not exist"),
            ErrorMessage(s"$tempDirectory/i/do/not/exist is not a directory"))),
        RequiredOptionIsMissing(OptionName("--studentify-base-directory, -s")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", realFile, "-s", nonExistentDirectory(tempDirectory)),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(ErrorMessage(s"$realFile is not a directory"), ErrorMessage(s"$realFile is not in a git repository"))),
        FailedToValidateArgument(
          OptionName("s"),
          List(
            ErrorMessage(s"$tempDirectory/i/do/not/exist does not exist"),
            ErrorMessage(s"$tempDirectory/i/do/not/exist is not a directory"))),
        RequiredOptionIsMissing(OptionName("--studentify-base-directory, -s")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", tempDirectory.getAbsolutePath, "-s", secondRealDirectory),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(ErrorMessage(s"${tempDirectory.getAbsolutePath} is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))))

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-m", baseDirectoryGitRoot.getAbsolutePath, "-s", secondRealDirectory),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(false),
        initGit = InitializeGitRepo(false),
        shared = SharedOptions(MainRepository(baseDirectoryGitRoot), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-s", secondRealDirectory, "--force-delete"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(false),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-s", secondRealDirectory, "-f"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(false),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-s", secondRealDirectory, "--init-git"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(false),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-s", secondRealDirectory, "-g"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(false),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-s", secondRealDirectory, "--force-delete", "--init-git"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-s", secondRealDirectory, "-f", "-g"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))))
}

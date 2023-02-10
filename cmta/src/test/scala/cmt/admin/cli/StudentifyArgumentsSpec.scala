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
import cmt.{ErrorMessage, FailedToValidateArgument, OptionName, RequiredOptionIsMissing}
import cmt.admin.command.Studentify
import cmt.admin.cli.ArgParsers.{
  initializeGitRepoArgParser,
  forceDeleteDestinationDirectoryArgParser,
  studentifyBaseDirectoryArgParser
}
import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, MainRepository, StudentifyBaseDirectory}
import cmt.support.{CommandLineArguments, TestDirectories}
import sbt.io.syntax.{File, file}

final class StudentifyArgumentsSpec extends CommandLineArgumentsSpec[Studentify.Options] with TestDirectories {

  val identifier = "studentify"

  val parser: Parser[Studentify.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = {
    val nonExistentFile = nonExistentDirectory(tempDirectory)
    invalidArgumentsTable(
      (
        Seq.empty,
        Set(
          RequiredOptionIsMissing(OptionName("--studentify-base-directory, -d")),
          RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
      (
        Seq("-m", nonExistentDirectory(tempDirectory), "-d", nonExistentDirectory(tempDirectory)),
        Set(
          FailedToValidateArgument(
            OptionName("m"),
            List(
              ErrorMessage(s"$nonExistentFile does not exist"),
              ErrorMessage(s"$nonExistentFile is not a directory"),
              ErrorMessage(s"$nonExistentFile is not in a git repository"))),
          FailedToValidateArgument(
            OptionName("d"),
            List(
              ErrorMessage(s"$nonExistentFile does not exist"),
              ErrorMessage(s"$nonExistentFile is not a directory"))),
          RequiredOptionIsMissing(OptionName("--studentify-base-directory, -d")),
          RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
      (
        Seq("-m", realFile, "-d", nonExistentDirectory(tempDirectory)),
        Set(
          FailedToValidateArgument(
            OptionName("m"),
            List(ErrorMessage(s"$realFile is not a directory"), ErrorMessage(s"$realFile is not in a git repository"))),
          FailedToValidateArgument(
            OptionName("d"),
            List(
              ErrorMessage(s"$nonExistentFile does not exist"),
              ErrorMessage(s"$nonExistentFile is not a directory"))),
          RequiredOptionIsMissing(OptionName("--studentify-base-directory, -d")),
          RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
      (
        Seq("-m", tempDirectory.getAbsolutePath, "-d", secondRealDirectory),
        Set(
          FailedToValidateArgument(
            OptionName("m"),
            List(ErrorMessage(s"${tempDirectory.getAbsolutePath} is not in a git repository"))),
          RequiredOptionIsMissing(OptionName("--main-repository, -m")))))
  }

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-m", baseDirectoryGitRoot.getAbsolutePath, "-d", secondRealDirectory),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(false),
        initGit = InitializeGitRepo(false),
        shared = SharedOptions(MainRepository(baseDirectoryGitRoot), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-d", secondRealDirectory, "--force-delete"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(false),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-d", secondRealDirectory, "-f"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(false),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-d", secondRealDirectory, "--init-git"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(false),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-d", secondRealDirectory, "-g"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(false),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-d", secondRealDirectory, "--force-delete", "--init-git"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))),
    (
      Seq("-m", firstRealDirectory, "-d", secondRealDirectory, "-f", "-g"),
      Studentify.Options(
        studentifyBaseDirectory = StudentifyBaseDirectory(file(secondRealDirectory)),
        forceDelete = ForceDeleteDestinationDirectory(true),
        initGit = InitializeGitRepo(true),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))))
}

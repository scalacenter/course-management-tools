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

import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, MainRepository, StudentifyBaseDirectory}
import cmt.admin.cli.CliCommand.Studentify
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object StudentifyArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "studentify"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (
      Seq(identifier),
      Seq(
        ReportError("Missing argument <Main repo>"),
        ReportError("Missing argument <studentified repo parent folder>"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory), nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"))),
    (
      Seq(identifier, realFile, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"$realFile is not a directory"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath, secondRealDirectory),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--force-delete"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-f"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--init-git"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        initializeAsGitRepo = InitializeGitRepo(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-g"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        initializeAsGitRepo = InitializeGitRepo(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--force-delete", "--init-git"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true),
        initializeAsGitRepo = InitializeGitRepo(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-f", "-g"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true),
        initializeAsGitRepo = InitializeGitRepo(true))))
}

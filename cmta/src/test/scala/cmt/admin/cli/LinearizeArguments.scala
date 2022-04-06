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

import cmt.Helpers
import cmt.admin.Domain.{ForceDeleteDestinationDirectory, LinearizeBaseDirectory, MainRepository}
import cmt.admin.cli.CliCommand.Linearize
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object LinearizeArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "linearize"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (
      Seq(identifier),
      Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, realFile),
      Seq(ReportError(s"$realFile is not a directory"), ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = validArgumentsTable[CliOptions](
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory),
      CliOptions.default(
        command = Linearize,
        mainRepository = MainRepository(baseDirectoryGitRoot),
        maybeLinearizeBaseFolder = Some(LinearizeBaseDirectory(file(secondRealDirectory))))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--force-delete"),
      CliOptions.default(
        command = Linearize,
        mainRepository = MainRepository(baseDirectoryGitRoot),
        maybeLinearizeBaseFolder = Some(LinearizeBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-f"),
      CliOptions.default(
        command = Linearize,
        mainRepository = MainRepository(baseDirectoryGitRoot),
        maybeLinearizeBaseFolder = Some(LinearizeBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))))
}

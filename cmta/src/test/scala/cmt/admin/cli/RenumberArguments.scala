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

import cmt.admin.Domain.{MainRepository, RenumberOffset, RenumberStart, RenumberStep}
import cmt.admin.cli.CliCommand.RenumberExercises
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object RenumberArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "renum"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (Seq(identifier), Seq(ReportError("Missing argument <Main repo>"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"))),
    (Seq(identifier, realFile), Seq(ReportError(s"$realFile is not a directory"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq(identifier, firstRealDirectory),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(file(".").getAbsoluteFile.getParentFile))),
    (
      Seq(identifier, firstRealDirectory, "--from", "9"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        maybeRenumberStart = Some(RenumberStart(9)))),
    (
      Seq(identifier, firstRealDirectory, "--to", "99"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        renumberOffset = RenumberOffset(99))),
    (
      Seq(identifier, firstRealDirectory, "--step", "999"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        renumberStep = RenumberStep(999))),
    (
      Seq(identifier, firstRealDirectory, "--from", "1", "--to", "2", "--step", "3"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        maybeRenumberStart = Some(RenumberStart(1)),
        renumberOffset = RenumberOffset(2),
        renumberStep = RenumberStep(3))))
}

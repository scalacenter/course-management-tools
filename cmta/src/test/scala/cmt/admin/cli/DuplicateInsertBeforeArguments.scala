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
import cmt.Helpers
import cmt.admin.Domain.{ExerciseNumber, MainRepository}
import cmt.admin.command.DuplicateInsertBefore
import cmt.support.{CommandLineArguments, TestDirectories}
import org.scalatest.prop.Tables
import sbt.io.syntax.File
import scopt.OEffect.ReportError
import cmt.admin.cli.ArgParsers.*
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}

object DuplicateInsertBeforeArguments
    extends CommandLineArguments[DuplicateInsertBefore.Options]
    with Tables
    with TestDirectories {

  val identifier = "dib"

  val parser: Parser[DuplicateInsertBefore.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()
//    (
//      Seq(identifier),
//      Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing option --exercise-number"))),
//    (
//      Seq(identifier, nonExistentDirectory(tempDirectory)),
//      Seq(
//        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
//        ReportError("Missing option --exercise-number"))),
//    (
//      Seq(identifier, realFile),
//      Seq(ReportError(s"$realFile is not a directory"), ReportError("Missing option --exercise-number"))),
//    (
//      Seq(identifier, tempDirectory.getAbsolutePath),
//      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = validArgumentsTable()
//    (
//      Seq(identifier, firstRealDirectory, "--exercise-number", "1"),
//      CliOptions.default(
//        command = DuplicateInsertBefore,
//        mainRepository = MainRepository(baseDirectoryGitRoot),
//        exerciseNumber = ExerciseNumber(1))))
}

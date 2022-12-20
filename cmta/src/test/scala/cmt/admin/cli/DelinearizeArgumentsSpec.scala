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
import cmt.{Helpers, OptionName, RequiredOptionIsMissing}
import cmt.admin.Domain.{LinearizeBaseDirectory, MainRepository}
import cmt.admin.command.Delinearize
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError
import cmt.admin.cli.ArgParsers.*

final class DelinearizeArgumentsSpec extends CommandLineArgumentsSpec[Delinearize.Options] with TestDirectories {

  val identifier = "delinearize"

  val parser: Parser[Delinearize.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (
      Seq.empty,
      Set(
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))))
//    (
//      Seq(identifier),
//      Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing argument linearized repo parent folder"))),
//    (
//      Seq(identifier, nonExistentDirectory(tempDirectory)),
//      Seq(
//        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
//        ReportError("Missing argument linearized repo parent folder"))),
//    (
//      Seq(identifier, realFile),
//      Seq(ReportError(s"$realFile is not a directory"), ReportError("Missing argument linearized repo parent folder"))),
//    (
//      Seq(identifier, tempDirectory.getAbsolutePath),
//      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = validArgumentsTable()
//    (
//      Seq(identifier, firstRealDirectory, secondRealDirectory),
//      CliOptions.default(
//        command = DeLinearize,
//        mainRepository = MainRepository(baseDirectoryGitRoot),
//        maybeLinearizeBaseFolder = Some(LinearizeBaseDirectory(file(secondRealDirectory))))))
}

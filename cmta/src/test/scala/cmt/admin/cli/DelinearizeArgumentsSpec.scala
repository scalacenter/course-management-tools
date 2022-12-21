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
import cmt.Helpers.adaptToOSSeparatorChar
import cmt.{ErrorMessage, FailedToValidateArgument, Helpers, OptionName, RequiredOptionIsMissing}
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

  def invalidArguments(tempDirectory: File) = {
    val nonExistentFile = nonExistentDirectory(tempDirectory)
    invalidArgumentsTable(
    (
      Seq.empty,
      Set(
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", nonExistentDirectory(tempDirectory)),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(
            ErrorMessage(s"$nonExistentFile does not exist"),
            ErrorMessage(s"$nonExistentFile is not a directory"),
            ErrorMessage(s"$nonExistentFile is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", realFile),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(ErrorMessage(s"$realFile is not a directory"), ErrorMessage(s"$realFile is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", realFile),
      Set(
        FailedToValidateArgument(
          OptionName("m"),
          List(ErrorMessage(s"$realFile is not a directory"), ErrorMessage(s"$realFile is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", tempDirectory.getAbsolutePath),
      Set(
        FailedToValidateArgument(OptionName("m"), List(ErrorMessage(s"$tempDirectory is not in a git repository"))),
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")),
        RequiredOptionIsMissing(OptionName("--main-repository, -m")))),
    (
      Seq("-m", baseDirectoryGitRoot.getAbsolutePath, "-l", realFile),
      Set(
        FailedToValidateArgument(OptionName("l"), List(ErrorMessage(s"$realFile is not a directory"))),
        RequiredOptionIsMissing(OptionName("--linearize-base-directory, -l")))))
  }

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-m", baseDirectoryGitRoot.getAbsolutePath, "-l", secondRealDirectory),
      Delinearize.Options(
        linearizeBaseDirectory = LinearizeBaseDirectory(file(secondRealDirectory)),
        shared = SharedOptions(MainRepository(baseDirectoryGitRoot), maybeConfigFile = None))))
}

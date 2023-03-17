package com.lunatech.cmt.admin.cli

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
import com.lunatech.cmt.{ErrorMessage, FailedToValidateArgument, OptionName, RequiredOptionIsMissing}
import com.lunatech.cmt.admin.Domain.{ExerciseNumber, MainRepository}
import com.lunatech.cmt.admin.command.DuplicateInsertBefore
import com.lunatech.cmt.support.TestDirectories
import sbt.io.syntax.{File, file}
import com.lunatech.cmt.admin.cli.ArgParsers.exerciseNumberArgParser

final class DuplicateInsertBeforeArgumentsSpec
    extends CommandLineArgumentsSpec[DuplicateInsertBefore.Options]
    with TestDirectories {

  val identifier = "dib"

  val parser: Parser[DuplicateInsertBefore.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = {
    val nonExistentFile = nonExistentDirectory(tempDirectory)
    invalidArgumentsTable(
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
              ErrorMessage(s"$nonExistentFile does not exist"),
              ErrorMessage(s"$nonExistentFile is not a directory"),
              ErrorMessage(s"$nonExistentFile is not in a git repository"))),
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
  }

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq("-m", firstRealDirectory, "--exercise-number", "1"),
      DuplicateInsertBefore.Options(
        exerciseNumber = ExerciseNumber(1),
        shared = SharedOptions(MainRepository(file(firstRealDirectory)), maybeConfigFile = None))))
}

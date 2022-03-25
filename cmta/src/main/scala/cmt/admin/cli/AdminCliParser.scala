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

import cmt.ValidationExtensions.*
import cmt.admin.Domain.*
import cmt.admin.cli.CliCommand.*
import cmt.Helpers
import sbt.io.syntax.File
import scopt.{OParser, OParserBuilder}
import cmt.ValidationExtensions.*
import cmt.core.cli.{CmdLineParseError, ScoptCliParser}

object AdminCliParser {

  def parse(args: Array[String]): Either[CmdLineParseError, CliOptions] =
    ScoptCliParser.parse(parser, CliOptions.default())(args)

  private val parser =
    given builder: OParserBuilder[CliOptions] = OParser.builder[CliOptions]
    import builder.*

    OParser.sequence(
      programName("cmta"),
      renumCmdParser,
      duplicateInsertBeforeParser,
      studentifyCmdParser,
      linearizeCmdParser,
      delinearizeCmdParser,
      configFileParser,
      versionParser,
      validateConfig)

  private def mainRepoArgument(using builder: OParserBuilder[CliOptions]): OParser[File, CliOptions] =
    import builder.*
    arg[File]("<Main repo>")
      .text("Root folder (or a subfolder thereof) the main repository")
      .validate(_.existsAndIsADirectoryInAGitRepository)
      .action { (mainRepository, options) =>
        val resolvedGitRoot = Helpers.resolveMainRepoPath(mainRepository).toOption.getOrElse(mainRepository)
        options.copy(mainRepository = MainRepository(resolvedGitRoot))
      }

  private def configFileParser(using builder: OParserBuilder[CliOptions]): OParser[File, CliOptions] =
    import builder.*
    opt[File]("configuration")
      .abbr("cfg")
      .text("CMT configuration file")
      .action((configurationFile, options) =>
        options.copy(maybeConfigurationFile = Some(ConfigurationFile(configurationFile))))

  private def duplicateInsertBeforeParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("dib")
      .text("Duplicate exercise and insert before")
      .action((_, options) => options.copy(command = DuplicateInsertBefore))
      .children(
        mainRepoArgument,
        opt[Int]("exercise-number")
          .required()
          .text("exercise number to duplicate")
          .abbr("n")
          .validate(_.isNotNegative)
          .action((exerciseNumber, options) => options.copy(exerciseNumber = ExerciseNumber(exerciseNumber))))

  private def linearizeCmdParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("linearize")
      .text("Generate a linearized repository from a given main repository")
      .action((_, options) => options.copy(command = Linearize))
      .children(
        mainRepoArgument,
        arg[File]("linearized repo parent folder")
          .text("Folder in which the linearized repository will be created")
          .validate(_.existsAndIsADirectory)
          .action((linearizeBaseDirectory, options) =>
            options.copy(maybeLinearizeBaseDirectory = Some(LinearizeBaseDirectory(linearizeBaseDirectory)))),
        opt[Unit]("force-delete")
          .text("Force-delete a pre-existing destination folder")
          .abbr("f")
          .action((_, options) =>
            options.copy(forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))))

  private def delinearizeCmdParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("delinearize")
      .text("De-linearize a linearized repository to its corresponding main repository")
      .action((_, options) => options.copy(command = DeLinearize))
      .children(
        mainRepoArgument,
        arg[File]("linearized repo parent folder")
          .text("Folder holding the linearized repository")
          .validate(_.existsAndIsADirectory)
          .action((linearizeBaseDirectory, options) =>
            options.copy(maybeLinearizeBaseDirectory = Some(LinearizeBaseDirectory(linearizeBaseDirectory)))))

  private def studentifyCmdParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("studentify")
      .text("Generate a studentified repository from a given main repository")
      .action((_, options) => options.copy(command = Studentify))
      .children(
        mainRepoArgument,
        arg[File]("<studentified repo parent folder>")
          .text("Folder in which the studentified repository will be created")
          .validate(_.existsAndIsADirectory)
          .action((studentifyDirectory, options) =>
            options.copy(maybeStudentifyBaseDirectory = Some(StudentifyBaseDirectory(studentifyDirectory)))),
        opt[Unit]("force-delete")
          .text("Force-delete a pre-existing destination folder")
          .abbr("f")
          .action((_, options) =>
            options.copy(forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))),
        opt[Unit]("init-git")
          .text("Initialize studentified repo as a git repo")
          .abbr("g")
          .action((_, options) => options.copy(initializeAsGitRepo = InitializeGitRepo(true))))

  private def renumCmdParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("renum")
      .text("Renumber exercises starting at a given offset and increment by a given step size")
      .action((_, options) => options.copy(command = RenumberExercises))
      .children(
        mainRepoArgument,
        opt[Int]("from")
          .text("Start renumbering from exercise number #")
          .validate(_.isNotNegative)
          .action((startAt, options) => options.copy(maybeRenumberStart = Some(RenumberStart(startAt)))),
        opt[Int]("to")
          .text("Renumber start offset (default=1)")
          .validate(_.isNotNegative)
          .action((offset, options) => options.copy(renumberOffset = RenumberOffset(offset))),
        opt[Int]("step")
          .text("Renumber step size (default=1)")
          .validate(_.isGreaterThanZero)
          .action((step, options) => options.copy(renumberStep = RenumberStep(step))))

  private def versionParser(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    cmd("version").text("Print version information").action((_, options) => options.copy(command = Version))

  private def validateConfig(using builder: OParserBuilder[CliOptions]): OParser[Unit, CliOptions] =
    import builder.*
    checkConfig(config =>
      config.command match
        case NoCommand => failure("missing command")
        case _         => success
    )
}

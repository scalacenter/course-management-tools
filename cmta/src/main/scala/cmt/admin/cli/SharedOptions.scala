package cmt.admin.cli

import caseapp.{ExtraName, Help, HelpMessage, Parser, ValueDescription}
import cmt.admin.Domain.{ConfigurationFile, MainRepository}
import cmt.admin.cli.ArgParsers.{configurationFileArgParser, mainRepositoryArgParser}

final case class SharedOptions(
    @ExtraName("m")
    @ValueDescription("The repository directory in/on which the command will operate")
    @HelpMessage("The path supplied must be a directory located within a git repository")
    mainRepository: MainRepository,
    @ExtraName("c")
    @ValueDescription("The (optional) configuration file to use during processing of the command")
    @HelpMessage(
      "if not specified will default to the config file present in the directory provided by the --main-repository argument")
    maybeConfigFile: Option[ConfigurationFile] = None)

object SharedOptions {
  implicit val parser: Parser[SharedOptions] = Parser.derive
  implicit val help: Help[SharedOptions] = Help.derive
}

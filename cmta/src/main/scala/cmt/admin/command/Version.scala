package cmt.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Name, Recurse, RemainingArgs, ValueDescription}
import cmt.CmtError
import cmt.core.cli.CmtCommand
import cmt.core.execution.Executable
import cmt.core.validation.Validatable
import cmt.printResult
import cmt.version.BuildInfo
object Version:
  @AppName("version")
  @CommandName("version")
  @HelpMessage(
    "'Studentifies' an existing repository - taking the 'main' repository and creating a CMT project in the target directory")
  final case class Options()

  given Validatable[Version.Options] with
    extension (options: Version.Options) def validated(): Either[CmtError, Version.Options] = Right(options)
  end given

  given Executable[Version.Options] with
    extension (options: Version.Options) def execute(): Either[CmtError, String] = Right(BuildInfo.toString)
  end given

  val command: CmtCommand[Version.Options] = new CmtCommand[Version.Options] {

    def run(options: Version.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

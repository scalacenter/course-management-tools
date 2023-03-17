package com.lunatech.cmt.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Name, Recurse, RemainingArgs, ValueDescription}
import com.lunatech.cmt.CmtError
import com.lunatech.cmt.core.cli.CmtCommand
import com.lunatech.cmt.core.execution.Executable
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.printResult
import com.lunatech.cmt.version.BuildInfo
object Version:
  @AppName("version")
  @CommandName("version")
  @HelpMessage("Print version info")
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

package coursemgmttools.admin.command

import caseapp.{AppName, CommandName, HelpMessage, RemainingArgs}
import coursemgmttools.CmtError
import coursemgmttools.core.cli.CmtCommand
import coursemgmttools.core.execution.Executable
import coursemgmttools.core.validation.Validatable
import coursemgmttools.printResult
import coursemgmttools.version.BuildInfo
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

package coursemgmttools.client.cli

import caseapp.Parser
import coursemgmttools.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import coursemgmttools.client.command.Install
import coursemgmttools.support.TestDirectories
import sbt.io.syntax.{File, file}
import coursemgmttools.core.cli.ArgParsers.given
import coursemgmttools.client.cli.ArgParsers.forceDeleteDestinationDirectoryArgParser

final class InstallArgumentsSpec extends CommandLineArgumentsSpec[Install.Options] with TestDirectories {

  val identifier = "install"

  val parser: Parser[Install.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (Seq("-s", baseDirectory.getAbsolutePath), Install.Options(LocalDirectory(baseDirectory))),
    (Seq("-s", zippedCourse), Install.Options(ZipFile(file(zippedCourse)))),
    (
      Seq("-s", "bleep/bloop/blop"),
      Install.Options(GithubProject(organisation = "bleep", project = "bloop", tag = Some("blop")))),
    (Seq("-s", "bleep/bloop"), Install.Options(GithubProject(organisation = "bleep", project = "bloop", tag = None))))
}

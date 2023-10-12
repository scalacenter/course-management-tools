package coursemgmt.client.cli

import caseapp.Parser
import coursemgmt.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import coursemgmt.client.command.Install
import coursemgmt.support.TestDirectories
import sbt.io.syntax.{File, file}
import coursemgmt.core.cli.ArgParsers.given
import coursemgmt.client.cli.ArgParsers.forceDeleteDestinationDirectoryArgParser

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

package cmt

import cmt.TestDirectories
import cmt.admin.cli.CliCommand.Version
import cmt.admin.cli.CliOptions
import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}

object VersionArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "version"

  def invalidArguments(tempDirectory: File) = Table(("args", "expectedErrors"))

  def validArguments(tempDirectory: File) =
    Table(("args", "expectedResult"), (Seq(identifier), CliOptions.default(command = Version)))
}

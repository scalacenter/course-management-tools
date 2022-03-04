package cmt

import cmt.client.cli.CliCommand.Version
import cmt.client.cli.CliOptions
import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}

object VersionArguments extends CommandLineArguments[CliOptions] with Tables {

  val identifier = "version"

  def invalidArguments(tempDirectory: File) = Table(("args", "expectedErrors"))

  def validArguments(tempDirectory: File) =
    Table(("args", "expectedResult"), (Seq(identifier), CliOptions.default(command = Version)))
}

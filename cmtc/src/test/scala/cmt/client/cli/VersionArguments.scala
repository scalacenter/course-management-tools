package cmt.client.cli

import cmt.client.cli.CliCommand.Version
import cmt.support.CommandLineArguments
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.File

object VersionArguments extends CommandLineArguments[CliOptions] with Tables {

  val identifier = "version"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) =
    validArgumentsTable((Seq(identifier), CliOptions.default(command = Version)))
}

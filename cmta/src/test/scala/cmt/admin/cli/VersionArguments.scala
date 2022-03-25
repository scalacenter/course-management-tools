package cmt.admin.cli

import cmt.admin.cli.CliCommand.Version
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.File

object VersionArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "version"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) =
    validArgumentsTable((Seq(identifier), CliOptions.default(command = Version)))
}

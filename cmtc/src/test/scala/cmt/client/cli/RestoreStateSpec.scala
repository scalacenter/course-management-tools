package cmt.client.cli

import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import cmt.support.{CommandLineArguments, TestDirectories}
import sbt.io.syntax.File

object RestoreStateSpec extends CommandLineArguments[CliOptions] with TestDirectories {

  val identifier = "restore-state"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable()
}

package cmt.client.cli

import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import cmt.support.{CommandLineArguments, TestDirectories}
import sbt.io.syntax.File

object PullSolutionSpec extends CommandLineArguments[CliOptions] with TestDirectories {

  val identifier = "pull-solution"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable()
}

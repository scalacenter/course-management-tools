package cmt.client.cli

import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import cmt.support.{CommandLineArguments, TestDirectories}
import sbt.io.syntax.File

object GotoExercise extends CommandLineArguments[CliOptions] with TestDirectories {

  val identifier = "goto-exercise"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable()
}

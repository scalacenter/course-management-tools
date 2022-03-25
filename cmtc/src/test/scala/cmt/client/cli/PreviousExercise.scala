package cmt.client.cli

import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import cmt.support.{CommandLineArguments, TestDirectories}
import sbt.io.syntax.File

object PreviousExercise extends CommandLineArguments[CliOptions] with TestDirectories {

  val identifier = "previous-exercise"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable()
}

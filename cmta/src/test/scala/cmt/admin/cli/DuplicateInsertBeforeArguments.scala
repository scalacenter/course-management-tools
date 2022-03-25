package cmt.admin.cli

import cmt.admin.Domain.ExerciseNumber
import cmt.admin.cli.CliCommand.DuplicateInsertBefore
import cmt.support.{CommandLineArguments, TestDirectories}
import org.scalatest.prop.Tables
import sbt.io.syntax.File
import scopt.OEffect.ReportError

object DuplicateInsertBeforeArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "dib"

  def invalidArguments(tempDirectory: File) = Table(
    ("args", "errors"),
    (
      Seq(identifier),
      Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing option --exercise-number"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError("Missing option --exercise-number"))),
    (
      Seq(identifier, realFile),
      Seq(ReportError(s"$realFile is not a directory"), ReportError("Missing option --exercise-number"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = Table(
    ("args", "expectedResult"),
    (
      Seq(identifier, firstRealDirectory, "--exercise-number", "1"),
      CliOptions.default(command = DuplicateInsertBefore, exerciseNumber = ExerciseNumber(1))))
}

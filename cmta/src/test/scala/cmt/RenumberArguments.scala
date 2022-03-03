package cmt

import cmt.TestDirectories
import cmt.admin.Domain.{MainRepository, RenumberOffset, RenumberStart, RenumberStep}
import cmt.admin.cli.CliCommand.RenumberExercises
import cmt.admin.cli.CliOptions
import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object RenumberArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "renum"

  def invalidArguments(tempDirectory: File) = Table(
    ("args", "errors"),
    (Seq(identifier), Seq(ReportError("Missing argument <Main repo>"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"))),
    (Seq(identifier, realFile), Seq(ReportError(s"$realFile is not a directory"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = Table(
    ("args", "expectedResult"),
    (
      Seq(identifier, firstRealDirectory),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(file(".").getAbsoluteFile.getParentFile))),
    (
      Seq(identifier, firstRealDirectory, "--from", "9"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        maybeRenumberStart = Some(RenumberStart(9)))),
    (
      Seq(identifier, firstRealDirectory, "--to", "99"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        renumberOffset = RenumberOffset(99))),
    (
      Seq(identifier, firstRealDirectory, "--step", "999"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        renumberStep = RenumberStep(999))),
    (
      Seq(identifier, firstRealDirectory, "--from", "1", "--to", "2", "--step", "3"),
      CliOptions.default(
        command = RenumberExercises,
        mainRepository = MainRepository(currentDirectory),
        maybeRenumberStart = Some(RenumberStart(1)),
        renumberOffset = RenumberOffset(2),
        renumberStep = RenumberStep(3))))
}

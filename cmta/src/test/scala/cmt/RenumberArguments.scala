package cmt

import cmt.TestDirectories.{firstRealDirectory, nonExistentDirectory, realFile, secondRealDirectory}
import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object RenumberArguments extends CommandLineArguments[CmtaOptions] with Tables {

  val identifier = "renum"

  def invalidArguments(tempDirectory: File) = Table(
    ("args", "errors"),
    (Seq(identifier), Seq(ReportError("Missing argument <Main repo>"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} is not a git repository"))),
    (
      Seq(identifier, realFile),
      Seq(ReportError(s"$realFile is not a directory"), ReportError(s"$realFile is not a git repository"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not a git repository"))))

  def validArguments(tempDirectory: File) = Table(
    ("args", "expectedResult"),
    (
      Seq(identifier, firstRealDirectory),
      CmtaOptions(
        file(".").getAbsoluteFile.getParentFile,
        RenumberExercises(startRenumAt = None, renumOffset = 1, renumStep = 1))),
    (
      Seq(identifier, firstRealDirectory, "--from", "9"),
      CmtaOptions(
        file(".").getAbsoluteFile.getParentFile,
        RenumberExercises(startRenumAt = Some(9), renumOffset = 1, renumStep = 1))),
    (
      Seq(identifier, firstRealDirectory, "--to", "99"),
      CmtaOptions(
        file(".").getAbsoluteFile.getParentFile,
        RenumberExercises(startRenumAt = None, renumOffset = 99, renumStep = 1))),
    (
      Seq(identifier, firstRealDirectory, "--step", "999"),
      CmtaOptions(
        file(".").getAbsoluteFile.getParentFile,
        RenumberExercises(startRenumAt = None, renumOffset = 1, renumStep = 999))),
    (
      Seq(identifier, firstRealDirectory, "--from", "1", "--to", "2", "--step", "3"),
      CmtaOptions(
        file(".").getAbsoluteFile.getParentFile,
        RenumberExercises(startRenumAt = Some(1), renumOffset = 2, renumStep = 3))))
}

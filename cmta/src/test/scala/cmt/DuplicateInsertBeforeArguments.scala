package cmt

import org.scalatest.prop.Tables
import scopt.OEffect.ReportError
import sbt.io.syntax.{File, file}
import cmt.TestDirectories.*

object DuplicateInsertBeforeArguments extends CommandLineArguments with Tables {

  val identifier = "dib"

  def invalidArguments(tempDirectory: File) = Table(
    ("args", "errors"),
    (Seq(identifier), Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing option --exercise-number"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} is not a git repository"),
        ReportError("Missing option --exercise-number"))),
    (
      Seq(identifier, realFile),
      Seq(
        ReportError(s"$realFile is not a directory"),
        ReportError(s"$realFile is not a git repository"),
        ReportError("Missing option --exercise-number"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not a git repository"))))

  def validArguments(tempDirectory: File) = Table(
    ("args", "expectedResult"),
    (
      Seq(identifier, firstRealDirectory, "--exercise-number", "1"),
      CmtaOptions(file(".").getAbsoluteFile.getParentFile, DuplicateInsertBefore(1))))
}

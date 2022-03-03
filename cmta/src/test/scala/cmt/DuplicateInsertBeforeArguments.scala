package cmt

import cmt.TestDirectories.*
import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object DuplicateInsertBeforeArguments extends CommandLineArguments[CmtaOptions] with Tables {

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
      CmtaOptions(file(".").getAbsoluteFile.getParentFile, DuplicateInsertBefore(1))))
}

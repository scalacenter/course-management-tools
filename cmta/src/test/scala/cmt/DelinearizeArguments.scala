package cmt

import cmt.TestDirectories.{firstRealDirectory, nonExistentDirectory, realFile, secondRealDirectory}
import cmt.support.CommandLineArguments
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.Tables
import org.scalatest.wordspec.AnyWordSpecLike
import sbt.io.IO
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object DelinearizeArguments extends CommandLineArguments[CmtaOptions] with Tables {

  val identifier = "delinearize"

  def invalidArguments(tempDirectory: File) = Table(
    ("args", "errors"),
    (
      Seq(identifier),
      Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} is not a git repository"),
        ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, realFile),
      Seq(
        ReportError(s"$realFile is not a directory"),
        ReportError(s"$realFile is not a git repository"),
        ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not a git repository"))))

  def validArguments(tempDirectory: File) = Table(
    ("args", "expectedResult"),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory),
      CmtaOptions(
        file(".").getAbsoluteFile.getParentFile,
        DeLinearize(linearizeBaseFolder = Some(file(secondRealDirectory))))))
}

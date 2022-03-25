package cmt.admin.cli

import cmt.admin.Domain.{LinearizeBaseDirectory, MainRepository}
import cmt.admin.cli.CliCommand.DeLinearize
import cmt.support.{CommandLineArguments, TestDirectories}
import cmt.support.CommandLineArguments.{invalidArgumentsTable, validArgumentsTable}
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object DelinearizeArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "delinearize"

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable(
    (
      Seq(identifier),
      Seq(ReportError("Missing argument <Main repo>"), ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, realFile),
      Seq(ReportError(s"$realFile is not a directory"), ReportError("Missing argument linearized repo parent folder"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory),
      CliOptions.default(
        command = DeLinearize,
        mainRepository = MainRepository(file(".").getAbsoluteFile.getParentFile),
        maybeLinearizeBaseFolder = Some(LinearizeBaseDirectory(file(secondRealDirectory))))))
}

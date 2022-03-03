package cmt

import cmt.TestDirectories
import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, MainRepository, StudentifyBaseDirectory}
import cmt.admin.cli.CliCommand.Studentify
import cmt.admin.cli.CliOptions
import cmt.support.CommandLineArguments
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.Tables
import org.scalatest.wordspec.AnyWordSpecLike
import sbt.io.IO
import sbt.io.syntax.{File, file}
import scopt.OEffect.ReportError

object StudentifyArguments extends CommandLineArguments[CliOptions] with Tables with TestDirectories {

  val identifier = "studentify"

  def invalidArguments(tempDirectory: File) = Table(
    ("args", "expectedErrors"),
    (
      Seq(identifier),
      Seq(
        ReportError("Missing argument <Main repo>"),
        ReportError("Missing argument <studentified repo parent folder>"))),
    (
      Seq(identifier, nonExistentDirectory(tempDirectory), nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"))),
    (
      Seq(identifier, realFile, nonExistentDirectory(tempDirectory)),
      Seq(
        ReportError(s"$realFile is not a directory"),
        ReportError(s"${nonExistentDirectory(tempDirectory)} does not exist"))),
    (
      Seq(identifier, tempDirectory.getAbsolutePath, secondRealDirectory),
      Seq(ReportError(s"${tempDirectory.getAbsolutePath} is not in a git repository"))))

  def validArguments(tempDirectory: File) = Table(
    ("args", "expectedResult"),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--force-delete"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-f"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--init-git"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        initializeAsGitRepo = InitializeGitRepo(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-g"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        initializeAsGitRepo = InitializeGitRepo(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "--force-delete", "--init-git"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true),
        initializeAsGitRepo = InitializeGitRepo(true))),
    (
      Seq(identifier, firstRealDirectory, secondRealDirectory, "-f", "-g"),
      CliOptions.default(
        command = Studentify,
        mainRepository = MainRepository(baseDirectory),
        maybeStudentifyBaseFolder = Some(StudentifyBaseDirectory(file(secondRealDirectory))),
        forceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(true),
        initializeAsGitRepo = InitializeGitRepo(true))))
}

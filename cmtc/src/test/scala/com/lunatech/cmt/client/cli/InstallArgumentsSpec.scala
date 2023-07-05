package com.lunatech.cmt.client.cli

import caseapp.Parser
import caseapp.core.Error.Other
import com.lunatech.cmt.client.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import com.lunatech.cmt.client.command.{GotoExercise, Install}
import com.lunatech.cmt.support.TestDirectories
import sbt.io.syntax.{File, file}
import com.lunatech.cmt.client.cli.ArgParsers.given

final class InstallArgumentsSpec extends CommandLineArgumentsSpec[Install.Options] with TestDirectories {

  val identifier = "install"

  val parser: Parser[Install.Options] = Parser.derive

  def invalidArguments(tempDirectory: File) = invalidArgumentsTable()

  def validArguments(tempDirectory: File) = validArgumentsTable(
    (Seq("-s", baseDirectory.getAbsolutePath), Install.Options(LocalDirectory(baseDirectory))),
    (Seq("-s", zippedCourse), Install.Options(ZipFile(file(zippedCourse)))),
    (Seq("-s", "bleep/bloop"), Install.Options(GithubProject(organisation = "bleep", project = "bloop"))))
}

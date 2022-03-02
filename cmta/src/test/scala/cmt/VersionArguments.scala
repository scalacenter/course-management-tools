package cmt

import cmt.TestDirectories.*
import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}

object VersionArguments extends CommandLineArguments[CmtaOptions] with Tables {

  val identifier = "version"

  def invalidArguments(tempDirectory: File) = Table(("args", "expectedErrors"))

  def validArguments(tempDirectory: File) =
    Table(("args", "expectedResult"), (Seq(identifier), CmtaOptions(file("."), Version)))
}

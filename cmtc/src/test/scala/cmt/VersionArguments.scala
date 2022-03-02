package cmt

import cmt.support.CommandLineArguments
import org.scalatest.prop.Tables
import sbt.io.syntax.{File, file}

object VersionArguments extends CommandLineArguments[CmtcOptions] with Tables {

  val identifier = "version"

  def invalidArguments(tempDirectory: File) = Table(("args", "expectedErrors"))

  def validArguments(tempDirectory: File) =
    Table(("args", "expectedResult"), (Seq(identifier), CmtcOptions(command = Version)))
}

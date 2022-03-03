package cmt

import sbt.io.syntax.File
import sbt.io.syntax.file

trait TestDirectories {

  def baseDirectory = file(".").getAbsoluteFile.getParentFile

  def nonExistentDirectory(tempDirectory: File) = s"${tempDirectory.getAbsolutePath}/i/do/not/exist"
  val firstRealDirectory = "./cmta/src/test/resources/i-am-a-directory"
  val secondRealDirectory = "./cmta/src/test/resources/i-am-another-directory"
  val realFile = "./cmta/src/test/resources/i-am-a-file.txt"

  val currentDirectory = file(".").getAbsoluteFile.getParentFile
}

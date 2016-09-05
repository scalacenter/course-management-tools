package com.lightbend

/**
  * Copyright © 2014, 2015, 2016 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import sbt.{IO => sbtio}

package object coursegentools {

  type Seq[+A] = scala.collection.immutable.Seq[A]
  val Seq = scala.collection.immutable.Seq

  case class StudentifyCmdOptions(masterRepo: File = new File("."), out: File = new File("."), multiJVM: Boolean = false)
  case class LinearizeCmdOptions(masterRepo: File = new File("."), linearRepo: File = new File("."), multiJVM: Boolean = false)
  case class DeLinearizeCmdOptions(masterRepo: File = new File("."), linearRepo: File = new File("."))

  case class ExNameAndSHA(exName: String, exSHA: String)

  def folderExists(folder: File): Boolean = {
    folder.exists() && folder.isDirectory
  }

  def dumpStringToFile(string: String, filePath: String): Unit = {
    Files.write(Paths.get(filePath), string.getBytes(StandardCharsets.UTF_8))
  }

  object FoldersOnly {
    def apply() = new FoldersOnly
  }
  class FoldersOnly extends java.io.FileFilter {
    override def accept(f: File): Boolean = f.isDirectory
  }

  object SbtTemplateFile {
    def apply() = new SbtTemplateFile
  }

  class SbtTemplateFile extends java.io.FileFilter {
    override def accept(f: File): Boolean = f.isFile && f.getName.endsWith(".sbt.template")
  }
}

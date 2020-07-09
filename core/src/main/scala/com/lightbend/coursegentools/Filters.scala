package com.lightbend.coursegentools

import java.io.File

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

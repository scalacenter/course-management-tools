package com.lunatech.cmt

import sbt.io.syntax.{File, file}

object Domain {

  final case class StudentifiedRepo(value: File)
  object StudentifiedRepo:
    val default: StudentifiedRepo = StudentifiedRepo(file(".").getAbsoluteFile.getParentFile)
}

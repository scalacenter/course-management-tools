package cmt.client

import sbt.io.syntax.{File, file}

object Domain:
  final case class ExerciseId(value: String)
  object ExerciseId:
    val default: ExerciseId = ExerciseId("")

  final case class StudentifiedRepo(value: File)
  object StudentifiedRepo:
    val default: StudentifiedRepo = StudentifiedRepo(file(".").getAbsoluteFile.getParentFile)

  final case class TemplatePath(value: String)
  object TemplatePath:
    val default: TemplatePath = TemplatePath("")

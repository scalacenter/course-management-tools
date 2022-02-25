package cmt

import sbt.io.syntax.*

object isExerciseFolder:
  def apply() = new isExerciseFolder

class isExerciseFolder() extends java.io.FileFilter:
  val ExerciseNameSpec = raw".*_\d{3}_\w+$$".r
  override def accept(f: File): Boolean =
    ExerciseNameSpec.findFirstIn(f.getPath).isDefined

package cmt

import sbt.io.syntax.*

object isExerciseFolder:
  def apply()(using CMTConfig) = new isExerciseFolder

class isExerciseFolder(using config: CMTConfig) extends java.io.FileFilter:
  val ExerciseNameSpec = s""".*[/\\\\]${config.mainRepoExercisePrefix}_\\d{3}_\\w+$$""".r
  override def accept(f: File): Boolean = ExerciseNameSpec.findFirstIn(f.getPath).isDefined


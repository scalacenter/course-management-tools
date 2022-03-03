package cmt.admin

import sbt.io.syntax.File

object Domain:

  final case class RenumberStart(value: Int)

  final case class RenumberOffset(value: Int)
  object RenumberOffset:
    val default: RenumberOffset = RenumberOffset(1)

  final case class RenumberStep(value: Int)
  object RenumberStep:
    val default: RenumberStep = RenumberStep(1)

  final case class ExerciseNumber(value: Int)
  object ExerciseNumber:
    val default: ExerciseNumber = ExerciseNumber(0)

  final case class StudentifyBaseDirectory(value: File)
  final case class ForceDeleteDestinationDirectory(value: Boolean)
  final case class InitializeGitRepo(value: Boolean)
  final case class LinearizeBaseDirectory(value: File)
  final case class MainRepository(value: File)
  final case class ConfigurationFile(value: File)

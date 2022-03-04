package cmt.client.command

import cmt.CMTcConfig
import cmt.client.Domain.{ExerciseId, StudentifiedRepo, TemplatePath}

sealed trait ClientCommand
object ClientCommand:
  final case class GotoFirstExercise(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class ListExercises(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class ListSavedStates(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  case object NoCommand extends ClientCommand
  final case class NextExercise(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class PreviousExercise(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class PullSolution(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class SaveState(config: CMTcConfig, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  case object Version extends ClientCommand
  final case class GotoExercise(config: CMTcConfig, studentifiedRepo: StudentifiedRepo, exerciseId: ExerciseId)
      extends ClientCommand
  final case class PullTemplate(config: CMTcConfig, studentifiedRepo: StudentifiedRepo, templatePath: TemplatePath)
      extends ClientCommand
  final case class RestoreState(config: CMTcConfig, studentifiedRepo: StudentifiedRepo, exerciseId: ExerciseId)
      extends ClientCommand

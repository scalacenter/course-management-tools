package cmt.client.command.execution

import cmt.client.Domain.ExerciseId
import cmt.client.command.ClientCommand.{GotoExercise, GotoFirstExercise}
import cmt.core.execution.Executable

given Executable[GotoFirstExercise] with
  extension (cmd: GotoFirstExercise)
    def execute(): Either[String, String] =
      GotoExercise(cmd.config, cmd.studentifiedRepo, ExerciseId(cmd.config.exercises.head)).execute()

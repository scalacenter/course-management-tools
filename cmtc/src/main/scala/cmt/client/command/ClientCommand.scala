package cmt.client.command

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import cmt.CMTcConfig
import cmt.client.Configuration
import cmt.client.Domain.{ExerciseId, StudentifiedRepo, TemplatePath}

sealed trait ClientCommand
object ClientCommand:
  final case class SetCurrentCourse(configuration: Configuration, studentifiedRepo: StudentifiedRepo)
      extends ClientCommand
  final case class GotoFirstExercise(configuration: Configuration, studentifiedRepo: StudentifiedRepo)
      extends ClientCommand
  final case class ListExercises(configuration: Configuration, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class ListSavedStates(configuration: Configuration, studentifiedRepo: StudentifiedRepo)
      extends ClientCommand
  case object NoCommand extends ClientCommand
  final case class NextExercise(configuration: Configuration, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class PreviousExercise(configuration: Configuration, studentifiedRepo: StudentifiedRepo)
      extends ClientCommand
  final case class PullSolution(configuration: Configuration, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  final case class SaveState(configuration: Configuration, studentifiedRepo: StudentifiedRepo) extends ClientCommand
  case object Version extends ClientCommand
  final case class GotoExercise(
      configuration: Configuration,
      studentifiedRepo: StudentifiedRepo,
      exerciseId: ExerciseId)
      extends ClientCommand
  final case class PullTemplate(
      configuration: Configuration,
      studentifiedRepo: StudentifiedRepo,
      templatePath: TemplatePath)
      extends ClientCommand
  final case class RestoreState(
      configuration: Configuration,
      studentifiedRepo: StudentifiedRepo,
      exerciseId: ExerciseId)
      extends ClientCommand

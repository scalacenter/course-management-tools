package cmt.client.cli

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
import cmt.client.Domain.{ExerciseId, StudentifiedRepo, TemplatePath}
import cmt.client.cli.CliCommand.*
import cmt.client.command.ClientCommand

sealed trait CliCommand
object CliCommand:
  case object GotoExercise extends CliCommand
  case object GotoFirstExercise extends CliCommand
  case object ListExercises extends CliCommand
  case object ListSavedStates extends CliCommand
  case object NextExercise extends CliCommand
  case object NoCommand extends CliCommand
  case object PreviousExercise extends CliCommand
  case object PullSolution extends CliCommand
  case object PullTemplate extends CliCommand
  case object RestoreState extends CliCommand
  case object SaveState extends CliCommand
  case object Version extends CliCommand

final case class CliOptions(
    command: CliCommand,
    studentifiedRepo: StudentifiedRepo,
    exerciseId: ExerciseId,
    templatePath: TemplatePath) {

  def toCommand: ClientCommand =
    command match
      case GotoExercise      => gotoExercise()
      case GotoFirstExercise => gotoFirstExercise()
      case ListExercises     => listExercises()
      case ListSavedStates   => listSavedStates()
      case NextExercise      => nextExercise()
      case NoCommand         => noCommand()
      case PreviousExercise  => previousExercise()
      case PullSolution      => pullSolution()
      case PullTemplate      => pullTemplate()
      case RestoreState      => restoreState()
      case SaveState         => saveState()
      case Version           => version()

  private def toConfig(): CMTcConfig =
    new CMTcConfig(studentifiedRepo.value)

  private def gotoExercise(): ClientCommand =
    ClientCommand.GotoExercise(toConfig(), studentifiedRepo, exerciseId)

  private def gotoFirstExercise(): ClientCommand =
    ClientCommand.GotoFirstExercise(toConfig(), studentifiedRepo)

  private def listExercises(): ClientCommand =
    ClientCommand.ListExercises(toConfig(), studentifiedRepo)

  private def listSavedStates(): ClientCommand =
    ClientCommand.ListSavedStates(toConfig(), studentifiedRepo)

  private def nextExercise(): ClientCommand =
    ClientCommand.NextExercise(toConfig(), studentifiedRepo)

  private def previousExercise(): ClientCommand =
    ClientCommand.PreviousExercise(toConfig(), studentifiedRepo)

  private def pullSolution(): ClientCommand =
    ClientCommand.PullSolution(toConfig(), studentifiedRepo)

  private def pullTemplate(): ClientCommand =
    ClientCommand.PullTemplate(toConfig(), studentifiedRepo, templatePath)

  private def restoreState(): ClientCommand =
    ClientCommand.RestoreState(toConfig(), studentifiedRepo, exerciseId)

  private def saveState(): ClientCommand =
    ClientCommand.SaveState(toConfig(), studentifiedRepo)

  private def noCommand(): ClientCommand =
    ClientCommand.NoCommand

  private def version(): ClientCommand =
    ClientCommand.Version
}
object CliOptions:
  def default(
      command: CliCommand = CliCommand.NoCommand,
      studentifiedRepo: StudentifiedRepo = StudentifiedRepo.default,
      exerciseId: ExerciseId = ExerciseId.default,
      template: TemplatePath = TemplatePath.default): CliOptions =
    CliOptions(command, studentifiedRepo, exerciseId, template)

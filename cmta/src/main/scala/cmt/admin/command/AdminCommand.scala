package cmt.admin.command

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

import cmt.CMTaConfig
import cmt.admin.Domain.*
import sbt.io.syntax.File

sealed trait AdminCommand
object AdminCommand:

  case object NoCommand extends AdminCommand

  case object Version extends AdminCommand

  final case class RenumberExercises(
      mainRepository: MainRepository,
      config: CMTaConfig,
      maybeStart: Option[RenumberStart],
      offset: RenumberOffset,
      step: RenumberStep)
      extends AdminCommand {

    val successMessage: String =
      s"Renumbered exercises in ${mainRepository.value.getPath} from $maybeStart to ${offset.value} by ${step.value}"
  }

  final case class DuplicateInsertBefore(
      mainRepository: MainRepository,
      config: CMTaConfig,
      exerciseNumber: ExerciseNumber)
      extends AdminCommand

  final case class Studentify(
      mainRepository: MainRepository,
      config: CMTaConfig,
      studentifyBaseDirectory: StudentifyBaseDirectory,
      forceDeleteDestinationDirectory: ForceDeleteDestinationDirectory,
      initializeAsGitRepo: InitializeGitRepo)
      extends AdminCommand

  final case class Linearize(
      mainRepository: MainRepository,
      config: CMTaConfig,
      linearizeBaseDirectory: LinearizeBaseDirectory,
      forceDeleteDestinationDirectory: ForceDeleteDestinationDirectory)
      extends AdminCommand

  final case class Delinearize(
      mainRepository: MainRepository,
      config: CMTaConfig,
      linearizeBaseDirectory: LinearizeBaseDirectory)
      extends AdminCommand

package cmt.admin.cli

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
import cmt.admin.cli.CliCommand.*
import cmt.admin.command.*
import sbt.io.syntax.{File, file}

sealed trait CliCommand
object CliCommand:
  case object NoCommand extends CliCommand
  case object Version extends CliCommand
  case object RenumberExercises extends CliCommand
  case object DuplicateInsertBefore extends CliCommand
  case object Studentify extends CliCommand
  case object Linearize extends CliCommand
  case object DeLinearize extends CliCommand

final case class CliOptions(
    command: CliCommand,
    maybeRenumberStart: Option[RenumberStart],
    renumberOffset: RenumberOffset,
    renumberStep: RenumberStep,
    exerciseNumber: ExerciseNumber,
    maybeStudentifyBaseDirectory: Option[StudentifyBaseDirectory],
    forceDeleteDestinationDirectory: ForceDeleteDestinationDirectory,
    initializeAsGitRepo: InitializeGitRepo,
    maybeLinearizeBaseDirectory: Option[LinearizeBaseDirectory],
    mainRepository: MainRepository,
    maybeConfigurationFile: Option[ConfigurationFile]) {

  def toCommand: AdminCommand =
    command match
      case CliCommand.RenumberExercises     => renumberExercises()
      case CliCommand.DuplicateInsertBefore => duplicateInsertBefore()
      case CliCommand.Studentify            => studentify()
      case CliCommand.Linearize             => linearize()
      case CliCommand.DeLinearize           => delinearize()
      case CliCommand.Version               => version()
      case CliCommand.NoCommand             => noCommand()

  private def toConfig(): CMTaConfig =
    new CMTaConfig(mainRepository.value, maybeConfigurationFile.map(_.value))

  private def renumberExercises(): AdminCommand =
    AdminCommand.RenumberExercises(mainRepository, toConfig(), maybeRenumberStart, renumberOffset, renumberStep)

  private def duplicateInsertBefore(): AdminCommand =
    AdminCommand.DuplicateInsertBefore(mainRepository, toConfig(), exerciseNumber)

  private def studentify(): AdminCommand =
    AdminCommand.Studentify(
      mainRepository,
      toConfig(),
      maybeStudentifyBaseDirectory.get,
      forceDeleteDestinationDirectory,
      initializeAsGitRepo)

  private def linearize(): AdminCommand =
    AdminCommand.Linearize(mainRepository, toConfig(), maybeLinearizeBaseDirectory.get, forceDeleteDestinationDirectory)

  private def delinearize(): AdminCommand =
    AdminCommand.Delinearize(mainRepository, toConfig(), maybeLinearizeBaseDirectory.get)

  private def noCommand(): AdminCommand =
    AdminCommand.NoCommand

  private def version(): AdminCommand =
    AdminCommand.Version
}
object CliOptions:
  def default(
      command: CliCommand = CliCommand.NoCommand,
      maybeRenumberStart: Option[RenumberStart] = None,
      renumberOffset: RenumberOffset = RenumberOffset.default,
      renumberStep: RenumberStep = RenumberStep.default,
      exerciseNumber: ExerciseNumber = ExerciseNumber.default,
      maybeStudentifyBaseFolder: Option[StudentifyBaseDirectory] = None,
      forceDeleteDestinationDirectory: ForceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(false),
      initializeAsGitRepo: InitializeGitRepo = InitializeGitRepo(false),
      maybeLinearizeBaseFolder: Option[LinearizeBaseDirectory] = None,
      mainRepository: MainRepository = MainRepository(file(".").getAbsoluteFile.getParentFile),
      maybeConfigurationFile: Option[ConfigurationFile] = None): CliOptions =
    CliOptions(
      command,
      maybeRenumberStart,
      renumberOffset,
      renumberStep,
      exerciseNumber,
      maybeStudentifyBaseFolder,
      forceDeleteDestinationDirectory,
      initializeAsGitRepo,
      maybeLinearizeBaseFolder,
      mainRepository,
      maybeConfigurationFile)

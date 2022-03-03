package cmt.admin.cli

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
      case RenumberExercises     => renumberExercises()
      case DuplicateInsertBefore => duplicateInsertBefore()
      case Studentify            => studentify()
      case Linearize             => linearize()
      case DeLinearize           => delinearize()
      case Version               => version()
      case NoCommand             => noCommand()

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

package cmt.admin.command

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

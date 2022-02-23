package cmt

import scopt.OParser
import scopt.OParserBuilder

import sbt.io.syntax.*

sealed trait CmtaCommands
case object Missing extends CmtaCommands
final case class RenumberExercises(
    startRenumAt: Option[Int] = None,
    renumOffset: Int = 1,
    renumStep: Int = 1
) extends CmtaCommands
final case class DuplicateInsertBefore(exerciseNumber: Int = 0)
    extends CmtaCommands
final case class Studentify(
    studentifyBaseFolder: Option[File] = None,
    forceDeleteExistingDestinationFolder: Boolean = false,
    initializeAsGitRepo: Boolean = false
) extends CmtaCommands
final case class Linearize(
    linearizeBaseFolder: Option[File] = None,
    forceDeleteExistingDestinationFolder: Boolean = false
) extends CmtaCommands
final case class DeLinearize(linearizeBaseFolder: Option[File] = None)
    extends CmtaCommands

final case class CmtaOptions(
    mainRepo: File = new File("."),
    command: CmtaCommands = Missing,
    configFile: Option[File] = None
)

val cmtaParser = {
  given builder: OParserBuilder[CmtaOptions] = OParser.builder[CmtaOptions]
  import builder.*

  OParser.sequence(
    programName("cmta"),
    renumCmdParser,
    duplicateInsertBeforeParser,
    studentifyCmdParser,
    linearizeCmdParser,
    delinearizeCmdParser,
    configFileParser,
    validateConfig
  )
}

private def mainRepoArgument(using
    builder: OParserBuilder[CmtaOptions]
): OParser[File, CmtaOptions] =
  import builder.*
  arg[File]("<Main repo>")
    .text("Root folder (or a subfolder thereof) the main repository")
    .validate { f =>
      if f.isDirectory then success
      else failure(s"$f is not a directory")
    }
    .action { (mainRepo, c) =>
      Helpers.resolveMainRepoPath(mainRepo) match {
        case Right(path) =>
          c.copy(mainRepo = path)
        case Left(msg) =>
          printError(s"$mainRepo is not a git repository"); ???
      }
    }

private def configFileParser(using
    builder: OParserBuilder[CmtaOptions]
): OParser[File, CmtaOptions] =
  import builder.*
  opt[File]("configuration")
    .abbr("cfg")
    .text("CMT configuration file")
    .action { (configFile, c) =>
      c.copy(configFile = Some(configFile))
    }

private def duplicateInsertBeforeParser(using
    builder: OParserBuilder[CmtaOptions]
): OParser[Unit, CmtaOptions] =
  import builder.*
  cmd("dib")
    .text("Duplicate exercise and insert before")
    .action { (_, c) =>
      c.copy(command = DuplicateInsertBefore())
    }
    .children(
      mainRepoArgument,
      opt[Int]("exercise-number")
        .required()
        .text("exercise number to duplicate")
        .abbr("-n")
        .action {
          case (n, c @ CmtaOptions(mainRepo, x: DuplicateInsertBefore, _)) =>
            c.copy(command = x.copy(exerciseNumber = n))
          case (n, c) =>
            c.copy(command = DuplicateInsertBefore(exerciseNumber = n))
        }
    )

private def linearizeCmdParser(using
    builder: OParserBuilder[CmtaOptions]
): OParser[Unit, CmtaOptions] =
  import builder.*
  cmd("linearize")
    .text("Generate a linearized repository from a given main repository")
    .action { (_, c) =>
      c.copy(command = Linearize())
    }
    .children(
      mainRepoArgument,
      arg[File]("linearized repo parent folder")
        .text("Folder in which the linearized repository will be created")
        .validate { baseFolder =>
          (baseFolder.exists, baseFolder.isDirectory) match
            case (true, true) => success
            case (false, _)   => failure(s"${baseFolder.getPath} doesn't exist")
            case (_, false) =>
              failure(s"${baseFolder.getPath} is not a directory")
        }
        .action {
          case (linRepo, c @ CmtaOptions(mainRepo, x: Linearize, _)) =>
            c.copy(command = x.copy(linearizeBaseFolder = Some(linRepo)))
          case (linRepo, c) =>
            c.copy(command = Linearize(linearizeBaseFolder = Some(linRepo)))
        },
      opt[Unit]("force-delete")
        .text("Force-delete a pre-existing destination folder")
        .abbr("f")
        .action {
          case (_, c @ CmtaOptions(mainRepo, x: Linearize, _)) =>
            c.copy(command =
              x.copy(forceDeleteExistingDestinationFolder = true)
            )
          case (_, c) =>
            c.copy(command =
              Linearize(forceDeleteExistingDestinationFolder = true)
            )
        }
    )

private def delinearizeCmdParser(using
    builder: OParserBuilder[CmtaOptions]
): OParser[Unit, CmtaOptions] =
  import builder.*
  cmd("delinearize")
    .text(
      "De-linearize a linearized repository to its corresponding main repository"
    )
    .action { (_, c) =>
      c.copy(command = DeLinearize())
    }
    .children(
      mainRepoArgument,
      arg[File]("linearized repo parent folder")
        .text("Folder holding the linearized repository")
        .validate { baseFolder =>
          (baseFolder.exists, baseFolder.isDirectory) match
            case (true, true) => success
            case (false, _)   => failure(s"${baseFolder.getPath} doesn't exist")
            case (_, false) =>
              failure(s"${baseFolder.getPath} is not a directory")
        }
        .action {
          case (linRepo, c @ CmtaOptions(mainRepo, x: DeLinearize, _)) =>
            c.copy(command = x.copy(linearizeBaseFolder = Some(linRepo)))
          case (linRepo, c) =>
            c.copy(command = Linearize(Some(linRepo)))
        }
    )

private def studentifyCmdParser(using
    builder: OParserBuilder[CmtaOptions]
): OParser[Unit, CmtaOptions] =
  import builder.*
  cmd("studentify")
    .text("Generate a studentified repository from a given main repository")
    .action { (_, c) =>
      c.copy(command = Studentify())
    }
    .children(
      mainRepoArgument,
      arg[File]("<studentified repo parent folder>")
        .text("Folder in which the studentified repository will be created")
        .validate { baseFolder =>
          (baseFolder.exists, baseFolder.isDirectory) match
            case (true, true) => success
            case (false, _)   => failure(s"${baseFolder.getPath} doesn't exist")
            case (_, false) =>
              failure(s"${baseFolder.getPath} is not a directory")
        }
        .action {
          case (studRepo, c @ CmtaOptions(_, x: Studentify, _)) =>
            c.copy(command = x.copy(studentifyBaseFolder = Some(studRepo)))
          case (studRepo, c) =>
            c.copy(command = Studentify(studentifyBaseFolder = Some(studRepo)))
        },
      opt[Unit]("force-delete")
        .text("Force-delete a pre-existing destination folder")
        .abbr("f")
        .action {
          case (_, c @ CmtaOptions(mainRepo, x: Studentify, _)) =>
            c.copy(command =
              x.copy(forceDeleteExistingDestinationFolder = true)
            )
          case (_, c) =>
            c.copy(command =
              Studentify(forceDeleteExistingDestinationFolder = true)
            )
        },
      opt[Unit]("init-git")
        .text("Initialize studentified repo as a git repo")
        .abbr("g")
        .action {
          case (_, c @ CmtaOptions(mainRepo, x: Studentify, _)) =>
            c.copy(command = x.copy(initializeAsGitRepo = true))
          case (_, c) =>
            c.copy(command = Studentify(initializeAsGitRepo = true))
        }
    )

private def renumCmdParser(using
    builder: OParserBuilder[CmtaOptions]
): OParser[Unit, CmtaOptions] =
  import builder.*
  cmd("renum")
    .text(
      "Renumber exercises starting at a given offset and increment by a given step size"
    )
    .action { (_, c) =>
      c.copy(command = RenumberExercises())
    }
    .children(
      mainRepoArgument,
      opt[Int]("start-renumber-at")
        .text("Number of exercise to start renumbering")
        .abbr("s")
        .validate(startAt =>
          if startAt >= 0 then success
          else failure(s"renumber start exercise number should be >= 0")
        )
        .action {
          case (
                startAt,
                c @ CmtaOptions(_, RenumberExercises(_, offset, step), _)
              ) =>
            c.copy(command = RenumberExercises(Some(startAt), offset, step))
          case (startAt, c) =>
            c.copy(command = RenumberExercises(startRenumAt = Some(startAt)))
        },
      opt[Int]("offset")
        .text("Renumber start offset (default=1)")
        .abbr("o")
        .validate(offset =>
          if offset >= 0 then success
          else failure(s"renumber offset should be >= 0")
        )
        .action {
          case (
                offset,
                c @ CmtaOptions(_, RenumberExercises(startAt, _, step), _)
              ) =>
            c.copy(command = RenumberExercises(startAt, offset, step))
          case (offset, c) =>
            c.copy(command = RenumberExercises(renumOffset = offset))
        },
      opt[Int]("step")
        .text("Renumber step size (default=1)")
        .abbr("s")
        .validate(step =>
          if step >= 1 then success
          else failure(s"renumber step size should be >= 1")
        )
        .action {
          case (
                step,
                c @ CmtaOptions(_, RenumberExercises(startAt, offset, _), _)
              ) =>
            c.copy(command = RenumberExercises(startAt, offset, step))
          case (step, c) =>
            c.copy(command = RenumberExercises(renumStep = step))
        }
    )

private def validateConfig(using
    builder: OParserBuilder[CmtaOptions]
): OParser[Unit, CmtaOptions] =
  import builder.*
  checkConfig(config =>
    config.command match
      case Missing => failure("missing command")
      case _       => success
  )

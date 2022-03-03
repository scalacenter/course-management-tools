package cmt

import cmt.ValidationExtensions.*
import sbt.io.syntax.*
import scopt.{OParser, OParserBuilder}

sealed trait CmtcCommands
case object Missing extends CmtcCommands
case object Version extends CmtcCommands
case object PullSolution extends CmtcCommands
final case class RestoreState(exerciseID: Option[String] = None) extends CmtcCommands
case object ListExercises extends CmtcCommands
case object NextExercise extends CmtcCommands
final case class GotoExercise(exerciseID: Option[String] = None) extends CmtcCommands
case object GotoFirstExercise extends cmt.CmtcCommands
case object ListSavedStates extends CmtcCommands
case object SaveState extends CmtcCommands
case object PreviousExercise extends CmtcCommands
final case class PullTemplate(template: Option[String] = None) extends CmtcCommands

final case class CmtcOptions(command: CmtcCommands = Missing, studentifiedRepo: Option[File] = None)

val parser = {
  given builder: OParserBuilder[CmtcOptions] = OParser.builder[CmtcOptions]
  import builder.*

  OParser.sequence(
    programName("cmtc"),
    pullSolutionParser,
    listExercisesParser,
    gotoExerciseParser,
    gotoFirstExerciseParser,
    previousExerciseParser,
    nextExerciseParser,
    pullTemplateParser,
    saveStateParser,
    restoreStateParser,
    savedStateParser,
    versionParser,
    validateConfig)
}

private def previousExerciseParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("previous-exercise")
    .text("Move to previous exercise and pull in tests for that exercise")
    .action { (_, c) => c.copy(command = PreviousExercise) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })
end previousExerciseParser

private def nextExerciseParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("next-exercise")
    .text("Move to next exercise and pull in tests for that exercise")
    .action { (_, c) => c.copy(command = NextExercise) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })
end nextExerciseParser

private def listExercisesParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("list-exercises")
    .text("List all exercises")
    .action { (_, c) => c.copy(command = ListExercises) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })

private def pullTemplateParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("pull-template")
    .text("Pull a template into the current exercise state")
    .children(
      arg[String]("<template path>").action { case (templatePath, c) =>
        c.copy(command = PullTemplate(Some(templatePath)))
      },
      arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
        c.copy(studentifiedRepo = Some(repo))
      })

private def gotoExerciseParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("goto-exercise")
    .text("Go to a given exercise and fetch corresponding tests")
    .children(
      arg[String]("<exercise ID>").action { case (exercise, c) =>
        c.copy(command = GotoExercise(Some(exercise)))
      },
      arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
        c.copy(studentifiedRepo = Some(repo))
      })

private def gotoFirstExerciseParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("goto-first-exercise")
    .text("Go to the first exercise and fetch corresponding tests")
    .action { (_, c) => c.copy(command = GotoFirstExercise) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })

private def pullSolutionParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("pull-solution")
    .text("Pull solution for a given exercise")
    .action { (_, c) => c.copy(command = PullSolution) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })

private def saveStateParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("save-state")
    .text("Save state of current exercise")
    .action { (_, c) => c.copy(command = SaveState) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })

private def savedStateParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("list-saved-states")
    .text("List all saved exercise states")
    .action { (_, c) => c.copy(command = ListSavedStates) }
    .children(arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
      c.copy(studentifiedRepo = Some(repo))
    })

private def restoreStateParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("restore-state")
    .text("Restore state of a previously save exercise state")
    .children(
      arg[String]("<exercise ID>").action { case (exercise, c) =>
        c.copy(command = RestoreState(Some(exercise)))
      },
      arg[File]("<studentified repo  folder>").validate(_.existsAndIsADirectory).action { (repo, c) =>
        c.copy(studentifiedRepo = Some(repo))
      })

private def versionParser(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("version").text("Print version information").action { (_, c) =>
    c.copy(command = Version)
  }

private def validateConfig(using builder: OParserBuilder[CmtcOptions]): OParser[Unit, CmtcOptions] =
  import builder.*
  checkConfig(config =>
    config.command match
      case Missing           => failure("missing command")
      case PullSolution      => success
      case ListExercises     => success
      case NextExercise      => success
      case PreviousExercise  => success
      case SaveState         => success
      case ListSavedStates   => success
      case _: RestoreState   => success
      case _: GotoExercise   => success
      case GotoFirstExercise => success
      case _: PullTemplate   => success
      case Version           => success
  )

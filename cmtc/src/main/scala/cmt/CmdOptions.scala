package cmt

import scopt.OParser
import scopt.OParserBuilder

import sbt.io.syntax.*

sealed trait CmtcCommands
case object Missing extends CmtcCommands
final case class PullSolution(exerciseID: Option[String] = None)
    extends CmtcCommands
case object ListExercises extends CmtcCommands
case object NextExercise extends CmtcCommands
case object PreviousExercise extends CmtcCommands

final case class CmtcOptions(
    command: CmtcCommands = Missing,
    studentifiedRepo: Option[File] = None
)

val parser = {
  given builder: OParserBuilder[CmtcOptions] = OParser.builder[CmtcOptions]
  import builder.*

  OParser.sequence(
    programName("cmtc"),
    pullSolutionParser,
    listExercisesParser,
    previousExerciseParser,
    nextExerciseParser,
    validateConfig
  )
}

private def previousExerciseParser(using
    builder: OParserBuilder[CmtcOptions]
): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("previous-exercise")
    .text("Move to previous exercise and pull in tests for that exercise")
    .action { (_, c) => c.copy(command = PreviousExercise) }
    .children(
      arg[File]("<studentified repo  folder>")
        .validate { studentifiedFolder =>
          if studentifiedFolder.exists
          then success
          else failure(s"$studentifiedFolder: doesn't exist")
        }
        .action { (repo, c) =>
          c.copy(studentifiedRepo = Some(repo))
        }
    )
end previousExerciseParser

private def nextExerciseParser(using
    builder: OParserBuilder[CmtcOptions]
): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("next-exercise")
    .text("Move to next exercise and pull in tests for that exercise")
    .action { (_, c) => c.copy(command = NextExercise) }
    .children(
      arg[File]("<studentified repo  folder>")
        .validate { studentifiedFolder =>
          if studentifiedFolder.exists
          then success
          else failure(s"$studentifiedFolder: doesn't exist")
        }
        .action { (repo, c) =>
          c.copy(studentifiedRepo = Some(repo))
        }
    )
end nextExerciseParser

private def listExercisesParser(using
    builder: OParserBuilder[CmtcOptions]
): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("list-exercises")
    .text("List all exercises")
    .action { (_, c) => c.copy(command = ListExercises) }
    .children(
      arg[File]("<studentified repo  folder>")
        .validate { studentifiedFolder =>
          if studentifiedFolder.exists
          then success
          else failure(s"$studentifiedFolder: doesn't exist")
        }
        .action { (repo, c) =>
          c.copy(studentifiedRepo = Some(repo))
        }
    )

private def pullSolutionParser(using
    builder: OParserBuilder[CmtcOptions]
): OParser[Unit, CmtcOptions] =
  import builder.*
  cmd("pull-solution")
    .text("Pull solution for a given exercise")
    .children(
      arg[String]("<exercise ID>")
        .action { case (exercise, c) =>
          c.copy(command = PullSolution(Some(exercise)))
        },
      arg[File]("<studentified repo  folder>")
        .validate { studentifiedFolder =>
          if studentifiedFolder.exists
          then success
          else failure(s"$studentifiedFolder: doesn't exist")
        }
        .action { (repo, c) =>
          c.copy(studentifiedRepo = Some(repo))
        }
    )

private def validateConfig(using
    builder: OParserBuilder[CmtcOptions]
): OParser[Unit, CmtcOptions] =
  import builder.*
  checkConfig(config =>
    config.command match
      case Missing          => failure("missing command")
      case _: PullSolution  => success
      case ListExercises    => success
      case NextExercise     => success
      case PreviousExercise => success
  )

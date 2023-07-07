package com.lunatech.cmt.client.cli

import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import com.lunatech.cmt.client.Domain.{
  ExerciseId,
  ForceMoveToExercise,
  TemplatePath
}
import cats.syntax.either.*

object ArgParsers {

  given forceMoveToExerciseArgParser: ArgParser[ForceMoveToExercise] =
    FlagArgParser.boolean.xmap[ForceMoveToExercise](_.forceMove, ForceMoveToExercise(_))

  given exerciseIdArgParser: ArgParser[ExerciseId] =
    SimpleArgParser.from[ExerciseId]("Exercise Id")(ExerciseId(_).asRight)

  given templatePathArgParser: ArgParser[TemplatePath] =
    SimpleArgParser.from[TemplatePath]("template path")(TemplatePath(_).asRight)
}

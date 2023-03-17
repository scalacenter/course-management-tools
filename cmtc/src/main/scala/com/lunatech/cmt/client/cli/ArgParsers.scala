package com.lunatech.cmt.client.cli

import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import com.lunatech.cmt.client.Domain.{ExerciseId, ForceMoveToExercise, StudentifiedRepo, TemplatePath}
import sbt.io.syntax.{File, file}
import cats.syntax.apply.*
import cats.syntax.either.*
import com.lunatech.cmt.core.validation.FileValidations.*

object ArgParsers {

  private val fileArgParser: ArgParser[File] =
    SimpleArgParser.from[File]("file")(file(_).asRight)

  given studentifiedRepoArgParser: ArgParser[StudentifiedRepo] =
    fileArgParser.xmapError[StudentifiedRepo](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => StudentifiedRepo(file))
          .leftMap(_.flatten)
          .toEither)

  given forceMoveToExerciseArgParser: ArgParser[ForceMoveToExercise] =
    FlagArgParser.boolean.xmap[ForceMoveToExercise](_.forceMove, ForceMoveToExercise(_))

  given exerciseIdArgParser: ArgParser[ExerciseId] =
    SimpleArgParser.from[ExerciseId]("Exercise Id")(ExerciseId(_).asRight)

  given templatePathArgParser: ArgParser[TemplatePath] =
    SimpleArgParser.from[TemplatePath]("template path")(TemplatePath(_).asRight)
}

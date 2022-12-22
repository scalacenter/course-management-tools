package cmt.client.cli

import caseapp.core.Error
import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise, StudentifiedRepo}
import sbt.io.syntax.{File, file}
import cats.syntax.apply.*
import cats.syntax.either.*
import cmt.core.validation.FileValidations.*

object ArgParsers {

  private val fileArgParser: ArgParser[File] =
    SimpleArgParser.from[File]("file")(file(_).asRight)

  implicit val studentifiedRepoArgParser: ArgParser[StudentifiedRepo] =
    fileArgParser.xmapError[StudentifiedRepo](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory, file.validateIsInAGitRepository)
          .mapN((_, _, _) => StudentifiedRepo(file))
          .leftMap(_.flatten)
          .toEither)

  implicit val forceMoveToExerciseArgParser: ArgParser[ForceMoveToExercise] =
    FlagArgParser.boolean.xmap[ForceMoveToExercise](_.forceMove, ForceMoveToExercise(_))

  private val intGreaterThanZero: ArgParser[Int] =
    SimpleArgParser.int.xmapError[Int](
      identity,
      int => if int < 0 then Error.Other(s"number must be 0 or greater, but received '$int'").asLeft else int.asRight)

  implicit val exerciseIdArgParser: ArgParser[ExerciseId] =
    intGreaterThanZero.xmap[ExerciseId](_.value, ExerciseId(_))
}

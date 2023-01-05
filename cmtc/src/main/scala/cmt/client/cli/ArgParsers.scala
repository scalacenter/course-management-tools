package cmt.client.cli

import caseapp.core.Error
import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import cmt.client.Domain.{ExerciseId, ForceMoveToExercise, GithubCourseRef, StudentifiedRepo, TemplatePath}
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
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => StudentifiedRepo(file))
          .leftMap(_.flatten)
          .toEither)

  implicit val forceMoveToExerciseArgParser: ArgParser[ForceMoveToExercise] =
    FlagArgParser.boolean.xmap[ForceMoveToExercise](_.forceMove, ForceMoveToExercise(_))

  implicit val exerciseIdArgParser: ArgParser[ExerciseId] =
    SimpleArgParser.from[ExerciseId]("Exercise Id")(ExerciseId(_).asRight)

  implicit val templatePathArgParser: ArgParser[TemplatePath] =
    SimpleArgParser.from[TemplatePath]("template path")(TemplatePath(_).asRight)

  implicit val githubCourseRefArgParser: ArgParser[GithubCourseRef] =
    SimpleArgParser.from[GithubCourseRef]("github course ref")(GithubCourseRef(_).asRight)
}

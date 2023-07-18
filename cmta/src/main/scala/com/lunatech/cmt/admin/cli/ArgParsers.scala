package com.lunatech.cmt.admin.cli

import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import cats.syntax.apply.*
import com.lunatech.cmt.admin.Domain.*
import com.lunatech.cmt.core.validation.FileValidations.*
import sbt.io.syntax.file
import caseapp.core.Error
import cats.syntax.either.*
import sbt.io.syntax.File

object ArgParsers:

  private val fileArgParser: ArgParser[File] =
    SimpleArgParser.from[File]("file")(file(_).asRight)

  given mainRepositoryArgParser: ArgParser[MainRepository] =
    fileArgParser.xmapError[MainRepository](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory, file.validateIsInAGitRepository)
          .mapN((_, _, _) => MainRepository(file))
          .leftMap(_.flatten)
          .toEither)

  given studentifyBaseDirectoryArgParser: ArgParser[StudentifyBaseDirectory] =
    fileArgParser.xmapError[StudentifyBaseDirectory](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => StudentifyBaseDirectory(file))
          .leftMap(_.flatten)
          .toEither)

  given linearizeBaseDirectoryArgParser: ArgParser[LinearizeBaseDirectory] =
    fileArgParser.xmapError[LinearizeBaseDirectory](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => LinearizeBaseDirectory(file))
          .leftMap(_.flatten)
          .toEither)

  given configurationFileArgParser: ArgParser[ConfigurationFile] =
    fileArgParser.xmapError[ConfigurationFile](
      _.value,
      file => (file.validateExists).map(_ => ConfigurationFile(file)).leftMap(_.flatten).toEither)

  given forceDeleteDestinationDirectoryArgParser: ArgParser[ForceDeleteDestinationDirectory] =
    FlagArgParser.boolean.xmap[ForceDeleteDestinationDirectory](_.value, ForceDeleteDestinationDirectory(_))

  given initializeGitRepoArgParser: ArgParser[InitializeGitRepo] =
    FlagArgParser.boolean.xmap[InitializeGitRepo](_.value, InitializeGitRepo(_))

  private val intGreaterThanZero: ArgParser[Int] =
    SimpleArgParser.int.xmapError[Int](
      identity,
      int => if int < 0 then Error.Other(s"number must be 0 or greater, but received '$int'").asLeft else int.asRight)

  given exerciseNumberArgParser: ArgParser[ExerciseNumber] =
    intGreaterThanZero.xmap[ExerciseNumber](_.value, ExerciseNumber(_))

  given renumberStartArgParser: ArgParser[RenumberStart] =
    intGreaterThanZero.xmap[RenumberStart](_.value, RenumberStart(_))

  given renumberStepArgParser: ArgParser[RenumberStep] =
    intGreaterThanZero.xmap[RenumberStep](_.value, RenumberStep(_))

  given renumberOffsetArgParser: ArgParser[RenumberOffset] =
    intGreaterThanZero.xmap[RenumberOffset](_.value, RenumberOffset(_))

  given courseTemplateArgParser: ArgParser[CourseTemplate] =
    SimpleArgParser.from[CourseTemplate]("Course Template")(str => CourseTemplate.fromString(str).asRight)

end ArgParsers

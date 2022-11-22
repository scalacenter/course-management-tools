package cmt.admin.cli

import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import cats.syntax.apply.*
import cmt.admin.Domain.*
import cmt.core.validation.FileValidations.*
import sbt.io.syntax.file
import caseapp.core.Error
import cats.syntax.either.*
import sbt.io.syntax.File

object ArgParsers:

  private val fileArgParser: ArgParser[File] =
    SimpleArgParser.from[File]("file")(file(_).asRight)

  implicit val mainRepositoryArgParser: ArgParser[MainRepository] =
    fileArgParser.xmapError[MainRepository](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory, file.validateIsInAGitRepository)
          .mapN((_, _, _) => MainRepository(file))
          .leftMap(_.flatten)
          .toEither)

  implicit val studentifyBaseDirectoryArgParser: ArgParser[StudentifyBaseDirectory] =
    fileArgParser.xmapError[StudentifyBaseDirectory](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => StudentifyBaseDirectory(file))
          .leftMap(_.flatten)
          .toEither)

  implicit val linearizeBaseDirectoryArgParser: ArgParser[LinearizeBaseDirectory] =
    fileArgParser.xmapError[LinearizeBaseDirectory](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => LinearizeBaseDirectory(file))
          .leftMap(_.flatten)
          .toEither)

  implicit val configurationFileArgParser: ArgParser[ConfigurationFile] =
    fileArgParser.xmapError[ConfigurationFile](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => ConfigurationFile(file))
          .leftMap(_.flatten)
          .toEither)

  implicit val forceDeleteDestinationDirectoryArgParser: ArgParser[ForceDeleteDestinationDirectory] =
    FlagArgParser.boolean.xmap[ForceDeleteDestinationDirectory](_.value, ForceDeleteDestinationDirectory(_))

  implicit val initializeGitRepoArgParser: ArgParser[InitializeGitRepo] =
    FlagArgParser.boolean.xmap[InitializeGitRepo](_.value, InitializeGitRepo(_))

  private val intGreaterThanZero: ArgParser[Int] =
    SimpleArgParser.int.xmapError[Int](
      identity,
      int => if int < 0 then Error.Other(s"number must be 0 or greater, but received '$int'").asLeft else int.asRight)

  implicit val exerciseNumberArgParser: ArgParser[ExerciseNumber] =
    intGreaterThanZero.xmap[ExerciseNumber](_.value, ExerciseNumber(_))

  implicit val renumberStartArgParser: ArgParser[RenumberStart] =
    intGreaterThanZero.xmap[RenumberStart](_.value, RenumberStart(_))

  implicit val renumberStepArgParser: ArgParser[RenumberStep] =
    intGreaterThanZero.xmap[RenumberStep](_.value, RenumberStep(_))

  implicit val renumberOffsetArgParser: ArgParser[RenumberOffset] =
    intGreaterThanZero.xmap[RenumberOffset](_.value, RenumberOffset(_))

end ArgParsers

package com.lunatech.cmt.client.cli

import caseapp.core.Error
import caseapp.core.Error.Other
import caseapp.core.argparser.{ArgParser, FlagArgParser, SimpleArgParser}
import com.lunatech.cmt.client.Domain.{
  ExerciseId,
  ForceMoveToExercise,
  InstallationSource,
  StudentifiedRepo,
  TemplatePath
}
import sbt.io.syntax.{File, file}
import cats.syntax.apply.*
import cats.syntax.either.*
import com.lunatech.cmt.client.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
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

  given installationSourceArgParser: ArgParser[InstallationSource] = {

    val githubProjectRegex = "([A-Za-z0-9-_]*)\\/([A-Za-z0-9-_]*)".r

    def toString(installationSource: InstallationSource): String =
      installationSource match {
        case LocalDirectory(value)                => value.getAbsolutePath()
        case ZipFile(value)                       => value.getAbsolutePath()
        case GithubProject(organisation, project) => s"$organisation/$project"
      }

    def fromString(str: String): Either[Error, InstallationSource] = {
      val maybeFile = file(str)
      val maybeGithub = str match {
        case githubProjectRegex(organisation, project) => Some(GithubProject(organisation, project))
        case _                                         => None
      }

      // is it a file? does it exist?
      //  yes
      //  - is it a directory?
      //    yes
      //    - it's a LocalDirectory
      //    no
      //    - does it end in '.zip'?
      //      yes
      //      - it's a ZipFile
      //      no
      //      - error - can't install from a file
      //  no
      //  - is it of the form a/b
      //    yes
      //    - it's a GithubProject
      //    no
      //    - error - i don't know what to do
      (maybeFile.exists(), maybeFile.isDirectory, str.endsWith(".zip"), maybeGithub) match {
        case (true, true, _, _)     => LocalDirectory(maybeFile).asRight
        case (true, false, true, _) => ZipFile(maybeFile).asRight
        case (true, false, false, _) =>
          Other(
            s"'$str' is a file but not a zip file - i'm afraid I don't know how to install a course from this file").asLeft
        case (false, _, _, Some(githubProject)) => githubProject.asRight
        case (_, _, _, _) =>
          Other(
            s"'$str' is not a local directory or zip file and it doesn't appear to be a Github project either. I'm afraid I don't know how to deal with this.").asLeft
      }
    }

    SimpleArgParser.string.xmapError[InstallationSource](toString, fromString)
  }
}

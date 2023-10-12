package coursemgmt.core.cli

import caseapp.core.Error
import caseapp.core.Error.Other
import caseapp.core.argparser.{ArgParser, SimpleArgParser}
import cats.syntax.apply.*
import cats.syntax.either.*
import coursemgmt.Domain.{InstallationSource, StudentifiedRepo}
import coursemgmt.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import coursemgmt.core.validation.FileValidations.*
import sbt.io.syntax.{File, file}

object ArgParsers:

  val fileArgParser: ArgParser[File] =
    SimpleArgParser.from[File]("file")(file(_).asRight)

  given studentifiedRepoArgParser: ArgParser[StudentifiedRepo] =
    fileArgParser.xmapError[StudentifiedRepo](
      _.value,
      file =>
        (file.validateExists, file.validateIsDirectory)
          .mapN((_, _) => StudentifiedRepo(file))
          .leftMap(_.flatten)
          .toEither)

  given installationSourceArgParser: ArgParser[InstallationSource] = {

    val githubProjectRegex = "([A-Za-z0-9-_]*)\\/([A-Za-z0-9-_]*)".r
    val githubProjectWithTagRegex = "([A-Za-z0-9-_]*)\\/([A-Za-z0-9-_]*)\\/(.*)".r

    def toString(installationSource: InstallationSource): String =
      installationSource match {
        case LocalDirectory(value)                           => value.getAbsolutePath()
        case ZipFile(value)                                  => value.getAbsolutePath()
        case GithubProject(organisation, project, None)      => s"$organisation/$project"
        case GithubProject(organisation, project, Some(tag)) => s"$organisation/$project/$tag"
      }

    def fromString(str: String): Either[Error, InstallationSource] = {
      val maybeFile = file(str)
      val maybeGithub = str match {
        case githubProjectRegex(organisation, project) => Some(GithubProject(organisation, project, None))
        case githubProjectWithTagRegex(organisation, project, tag) =>
          Some(GithubProject(organisation, project, Some(tag)))
        case _ => None
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

end ArgParsers

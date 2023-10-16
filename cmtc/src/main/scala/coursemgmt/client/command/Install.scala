package coursemgmt.client.command

import caseapp.*
import cats.syntax.either.*
import coursemgmt.*
import coursemgmt.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import coursemgmt.Domain.{InstallationSource, StudentifiedRepo}
import coursemgmt.Helpers.{findStudentRepoRoot, ignoreProcessStdOutStdErr}
import coursemgmt.client.Configuration
import coursemgmt.client.cli.CmtcCommand
import coursemgmt.core.cli.ArgParsers.installationSourceArgParser
import coursemgmt.core.cli.enforceNoTrailingArguments
import coursemgmt.core.validation.Validatable
import coursemgmt.client.Domain.ForceDeleteDestinationDirectory
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import coursemgmt.client.cli.ArgParsers.forceDeleteDestinationDirectoryArgParser
import coursemgmt.Releasables.{*, given}

import sys.process.*
import scala.util.{Failure, Success, Try}
import scala.util.Using

object Install:

  @AppName("install")
  @CommandName("install")
  @HelpMessage(
    "Install a course - from either a local directory, a zip file on the local file system or a Github project")
  final case class Options(
      @ExtraName("s")
      @ValueDescription("Source of the course, either a local folder or a zip file, or a Github project")
      source: InstallationSource,
      @ExtraName("f")
      @ValueDescription(
        "if set to 'true', a pre-existing installed course with the same name will be wiped before the new one is installed")
      forceDelete: ForceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(false))

  given Validatable[Install.Options] with
    extension (options: Install.Options)
      def validated(): Either[CmtError, Install.Options] =
        options.asRight
      end validated
  end given

  given Executable[Install.Options] with
    extension (cmd: Install.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        val forceDelete = cmd.forceDelete.value
        cmd.source match {
          case localDirectory: LocalDirectory =>
            installFromLocalDirectory(localDirectory, configuration, forceDelete)
          case zipFile: ZipFile => installFromZipFile(zipFile, configuration, forceDelete)
          case githubProject @ GithubProject(_, _, _) =>
            installFromGithubProject(githubProject, configuration, forceDelete)
        }

      private def installFromLocalDirectory(
          localDirectory: LocalDirectory,
          configuration: Configuration,
          forceDelete: Boolean): Either[CmtError, String] =
        for {
          studentRepoRoot <- findStudentRepoRoot(localDirectory.value)
          project = studentRepoRoot.getName
          _ <- checkPreExistingTargetFolder(project, configuration, forceDelete)
          _ = sbtio.move(studentRepoRoot, configuration.coursesDirectory.value / studentRepoRoot.getName)
          installCompletionMessage <- setCurrentCourse(project, configuration)
        } yield installCompletionMessage

      private def installFromZipFile(
          zipFile: ZipFile,
          configuration: Configuration,
          forceDelete: Boolean,
          deleteZipAfterInstall: Boolean = false): Either[CmtError, String] =
        val installResult = Using(TmpDir()) { case TmpDir(tmpDir) =>
          sbtio.unzip(zipFile.value, tmpDir)
          sbtio.delete(tmpDir / "__MACOSX") // Hack for MacOSX
          val zipRootFolders = sbtio.listFiles(tmpDir).to(Vector)
          if zipRootFolders.size == 1 then
            val project = zipRootFolders.head.getName
            val targetFolder = configuration.coursesDirectory.value / project
            (targetFolder.exists, forceDelete) match {
              case (false, _) | (true, true) =>
                sbtio.copyDirectory(tmpDir / project, targetFolder)
                sbtio.delete(tmpDir / project)
                if (deleteZipAfterInstall) {
                  sbtio.delete(zipFile.value)
                }
                setCurrentCourse(targetFolder.getName, configuration).map { currentCourseInfo =>
                  s"""Project $project successfully installed to $targetFolder
                       |
                       |$currentCourseInfo
                       |""".stripMargin
                }
              case (_, _) =>
                s"There is a pre-existing installed course for ${zipRootFolders.head.getName}".toExecuteCommandErrorMessage.asLeft
            }
          else s"Invalid CMT archive: ${zipFile.value.getName}".toExecuteCommandErrorMessage.asLeft
        }
        installResult match {
          case Success(Right(ok)) => ok.asRight[CmtError]
          case Success(failure)   => failure
          case Failure(e) =>
            s"""Unexpected error(${e.getMessage})
               |  ${zipFile.value} may be corrupt""".toExecuteCommandErrorMessage.asLeft
        }

      private def extractTag(lsFilesTagLine: String): String =
        lsFilesTagLine.replaceAll(""".*refs/tags/""", "")

      private def checkPreExistingTargetFolder(
          project: String,
          configuration: Configuration,
          forceDelete: Boolean): Either[CmtError, Unit] =
        val targetFolder = configuration.coursesDirectory.value / project
        val preExistingTargetFolder = targetFolder.exists()
        (preExistingTargetFolder, forceDelete) match {
          case (true, false) =>
            s"There is a pre-existing installed course for ${project}".toExecuteCommandErrorMessage.asLeft
          case (true, true) =>
            Right(sbtio.delete(targetFolder))
          case (false, _) =>
            Right(())
        }

      private def getProjectTags(gitPrefix: String, githubProject: GithubProject): Try[Seq[String]] =
        val cwd = file(".").getCanonicalFile
        val uri = s"${gitPrefix}${githubProject.organisation}/${githubProject.project}.git"
        val cmd = Seq("git", "-c", "versionsort.suffix=-", "ls-remote", "--tags", "--refs", "--sort", "v:refname", uri)
        Try(Process(cmd, cwd).!!(ignoreProcessStdOutStdErr).split("\n").to(Seq).map(extractTag))

      private def installFromGithubProject(
          githubProject: GithubProject,
          configuration: Configuration,
          forceDelete: Boolean): Either[CmtError, String] = {
        for {
          _ <- checkPreExistingTargetFolder(githubProject.project, configuration, forceDelete)
          installCompletionMessage <- {
            val maybeTags =
              for {
                tags <- getProjectTags("git@github.com:", githubProject).recoverWith(g_ =>
                  getProjectTags("https://github.com/", githubProject))
                trimmedTags = tags.map(_.trim())
              } yield trimmedTags
            val tags: Seq[String] = maybeTags match {
              case Success(s) => s
              case Failure(_) => Seq.empty[String]
            }

            val aTagWasPassedToInstall = githubProject.tag.isDefined
            val aTagWasPassedToInstallWhichMatchesARelease =
              githubProject.tag.isDefined && tags.contains(githubProject.tag.get)
            val maybeMostRecentTag = tags.lastOption

            (aTagWasPassedToInstall, aTagWasPassedToInstallWhichMatchesARelease, maybeMostRecentTag) match {
              case (false, _, Some(mostRecentTag)) =>
                downloadAndInstallStudentifiedRepo(githubProject, mostRecentTag, configuration, forceDelete)
              case (false, _, None) =>
                s"${githubProject.displayName}: Missing tag".toExecuteCommandErrorMessage.asLeft
              case (true, false, _) =>
                s"${githubProject.displayName}. ${githubProject.tag.get}: No such tag".toExecuteCommandErrorMessage.asLeft
              case (true, true, _) =>
                downloadAndInstallStudentifiedRepo(githubProject, githubProject.tag.get, configuration, forceDelete)
            }
          }
        } yield installCompletionMessage
      }

      private def downloadAndInstallStudentifiedRepo(
          githubProject: GithubProject,
          tag: String,
          configuration: Configuration,
          forceDelete: Boolean): Either[CmtError, String] =
        for {
          studentAssetUrl <- getStudentAssetUrl(githubProject, tag)
          _ = printMessage(s"Downloading studentified course from '$studentAssetUrl' to courses directory\n")
          downloadedZipFile <- downloadStudentAsset(studentAssetUrl, githubProject, configuration)
          _ <- installFromZipFile(downloadedZipFile, configuration, forceDelete, deleteZipAfterInstall = true)
          setCurrentCourseMessage <- setCurrentCourse(githubProject.project, configuration)
        } yield s"""Project ${githubProject.project} (${tag}) successfully installed to:
             |  ${configuration.coursesDirectory.value}/${githubProject.project}
             |
             |$setCurrentCourseMessage""".stripMargin

      private def setCurrentCourse(project: String, configuration: Configuration): Either[CmtError, String] = {
        val courseDirectory = configuration.coursesDirectory.value / project
        val studentifiedRepo = StudentifiedRepo(courseDirectory)
        SetCurrentCourse.Options(studentifiedRepo).execute(configuration)
      }

      private def getStudentAssetUrl(githubProject: GithubProject, tag: String): Either[CmtError, String] = {
        val organisation = githubProject.organisation
        val project = githubProject.project
        val url = s"https://github.com/${organisation}/${project}/releases/download/${tag}/${project}-student.zip"
        Right(url)
      }

      private def downloadStudentAsset(
          url: String,
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, ZipFile] = {
        val zipFile = ZipFile(configuration.coursesDirectory.value / s"${githubProject.project}.zip")
        for {
          _ <- downloadFile(url, zipFile)
        } yield zipFile
      }

      private def downloadFile(fileUri: String, destination: ZipFile): Either[CmtError, Unit] =
        val destinationPath = os.Path(Helpers.adaptToOSSeparatorChar(destination.value.getCanonicalPath()))
        Try(os.write.over(destinationPath, requests.get.stream(fileUri), createFolders = true)) match {
          case Success(()) => Right(())
          case Failure(e) =>
            s"""Failed to download asset: ${fileUri}
               |
               |${e.getStackTrace().mkString("\n")}
               |""".stripMargin.toExecuteCommandErrorMessage.asLeft
        }

    end extension
  end given

  val command = new CmtcCommand[Install.Options] {

    def run(options: Install.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end Install

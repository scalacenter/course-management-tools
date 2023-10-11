package coursemgmttools.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs, ValueDescription}
import coursemgmttools.{CmtError, printResult}
import coursemgmttools.admin.Domain.{ConfigurationFile, CourseTemplate}
import coursemgmttools.client.command.Executable
import coursemgmttools.core.validation.Validatable
import coursemgmttools.admin.cli.ArgParsers.{configurationFileArgParser, courseTemplateArgParser}
import coursemgmttools.client.Configuration
import coursemgmttools.client.cli.CmtcCommand
import coursemgmttools.Domain.InstallationSource.*
import coursemgmttools.Helpers.ignoreProcessStdOutStdErr
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import coursemgmttools.*

import cats.syntax.either.*

import java.io.FileFilter
import sys.process.*
import util.{Try, Success, Failure}

object New:

  private final case class TagSet(tags: Vector[String])

  @AppName("new")
  @CommandName("new")
  @HelpMessage(
    "Create a new course from an existing course template in a Github repository - by default the `lunatech-labs` organisation is used.")
  final case class Options(
      @ExtraName("t")
      @ValueDescription(
        "the template course to use - provide in the format 'organisation/project' or just 'project' if the project is in the lunatech-labs organisation on Github")
      template: CourseTemplate,
      @ExtraName("c")
      @ValueDescription("The (optional) configuration file to use during processing of the command")
      @HelpMessage(
        "if not specified will default to the config file present in the directory provided by the --main-repository argument")
      maybeConfigFile: Option[ConfigurationFile] = None)

  given Validatable[New.Options] with
    extension (options: New.Options)
      def validated(): Either[CmtError, New.Options] =
        Right(options)
  end given

  given Executable[New.Options] with
    extension (options: New.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        for {
          // list the contents of the ~/Courses directory, if there's anything already matching the
          // name then get the count so we can append to the name and prevent conflicts
          template <- options.template.value
          targetDirectoryName = createTargetDirectoryName(template, configuration)
          newRepo <- newCmtRepoFromGithubProject(template, targetDirectoryName, configuration)
        } yield newRepo

      private def createTargetDirectoryName(template: GithubProject, configuration: Configuration): String = {
        val existingFilesWithSameName = sbtio.listFiles(
          configuration.coursesDirectory.value,
          new FileFilter {
            override def accept(file: File): Boolean =
              file.name.startsWith(template.project)
          })
        val discriminator = if (existingFilesWithSameName.size > 0) s"-${existingFilesWithSameName.size}" else ""
        s"${template.project}$discriminator"
      }

      private def cloneMainRepo(githubProject: GithubProject, tmpDir: File): Either[CmtError, TagSet] =
        val project = githubProject.project
        val organisation = githubProject.organisation
        val tag = githubProject.tag
        val cloneGit = Process(Seq("git", "clone", s"git@github.com:$organisation/$project.git"), tmpDir)
        val cloneGh = Process(Seq("gh", "repo", "clone", s"$organisation/$project"), tmpDir)
        val cloneHttp = Process(Seq("git", "clone", s"https://github.com/$organisation/$project"), tmpDir)
        val cloneRepoStatus =
          Try(cloneGit.!).recoverWith(_ => Try(cloneGh.!)).recoverWith(_ => Try(cloneHttp.!)) match {
            case Success(x) =>
              if x == 0 then Right(x)
              else s"Cannot install from ${githubProject.displayName}: No such repo".toExecuteCommandErrorMessage.asLeft
            case Failure(_) =>
              s"Cannot install from ${githubProject.displayName}: No such repo".toExecuteCommandErrorMessage.asLeft
          }
        for {
          _ <- cloneRepoStatus
          tags = Process(Seq("git", "tag", "--sort", "v:refname"), tmpDir / project).!!.split("\n").to(Vector)
        } yield TagSet(tags)

      private def newCmtRepoFromGithubProject(
          githubProject: GithubProject,
          targetDirectoryName: String,
          configuration: Configuration): Either[CmtError, String] =
        val tmpDir = sbtio.createTemporaryDirectory
        val installResult = for {
          tagSet <- cloneMainRepo(githubProject, tmpDir)
          result <- downloadAndInstallRepo(githubProject, targetDirectoryName, configuration, tagSet, tmpDir)
        } yield result
        sbtio.delete(tmpDir)
        installResult

      private def copyRepo(
          githubProject: GithubProject,
          targetDirectoryName: String,
          configuration: Configuration,
          tag: String,
          tmpDir: File): Unit =
        Process(Seq("git", "checkout", tag), tmpDir / githubProject.project).!(ignoreProcessStdOutStdErr)
        sbtio.copyDirectory(tmpDir / githubProject.project, configuration.coursesDirectory.value / targetDirectoryName)
        sbtio.delete(configuration.coursesDirectory.value / targetDirectoryName / ".git")
        Helpers.initializeGitRepo(configuration.coursesDirectory.value / targetDirectoryName)
        val _ = Helpers.commitToGit("Initial commit", configuration.coursesDirectory.value / targetDirectoryName)

      private def downloadAndInstallRepo(
          githubProject: GithubProject,
          targetDirectoryName: String,
          configuration: Configuration,
          tagSet: TagSet,
          tmpDir: File): Either[CmtError, String] =
        (githubProject.tag, tagSet.tags.isEmpty, tagSet.tags.lastOption) match {
          case (None, _, Some(lastReleaseTag)) =>
            copyRepo(githubProject, targetDirectoryName, configuration, lastReleaseTag, tmpDir)
            Right(s"""Project:
                 |   ${githubProject.copy(tag = Some(lastReleaseTag)).displayName}
                 |successfully installed to:
                 |   ${configuration.coursesDirectory.value}/${targetDirectoryName}""".stripMargin)
          case (Some(tag), false, _) if tagSet.tags.contains(tag) =>
            copyRepo(githubProject, targetDirectoryName, configuration, tag, tmpDir)
            Right(s"""Project:
                 |   ${githubProject.displayName}
                 |successfully installed to:
                 |   ${configuration.coursesDirectory.value}/${targetDirectoryName}""".stripMargin)
          case (Some(tag), false, _) =>
            s"Cannot install from ${githubProject.displayName}. No such tag: $tag".toExecuteCommandErrorMessage.asLeft
          case (Some(_), true, _) | (None, _, None) =>
            s"Cannot install from ${githubProject.displayName}: No releases found".toExecuteCommandErrorMessage.asLeft
        }

  val command = new CmtcCommand[New.Options] {
    def run(options: New.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute(configuration)).printResult()
  }

end New

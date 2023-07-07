package com.lunatech.cmt.client.command

import caseapp.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.either.*
import com.lunatech.cmt.*
import com.lunatech.cmt.Domain.InstallationSource.{GithubProject, LocalDirectory, ZipFile}
import com.lunatech.cmt.Domain.{InstallationSource, StudentifiedRepo}
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.client.command.GithubSupport.*
import com.lunatech.cmt.core.cli.ArgParsers.installationSourceArgParser
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments
import com.lunatech.cmt.core.validation.Validatable
import github4s.Github
import github4s.domain.Release
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.{Client, JavaNetClientBuilder}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import java.io.File
import java.net.URL
import java.time.ZonedDateTime
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.sys.process.*

object Install:

  @AppName("install")
  @CommandName("install")
  @HelpMessage(
    "Install a course - from either a local directory, a zip file on the local file system or a Github project")
  final case class Options(
      @ExtraName("s")
      source: InstallationSource,
      @ExtraName("d")
      newName: Option[String] = None,
      @ExtraName("a")
      studentifiedAsset: Option[Boolean] = Some(true))

  given Validatable[Install.Options] with
    extension (options: Install.Options)
      def validated(): Either[CmtError, Install.Options] =
        options.asRight
      end validated
  end given

  given Executable[Install.Options] with
    extension (cmd: Install.Options)
      def execute(configuration: Configuration): Either[CmtError, String] =
        cmd.source match {
          case localDirectory: LocalDirectory => installFromLocalDirectory(localDirectory)
          case zipFile: ZipFile               => installFromZipFile(zipFile, configuration)
          case githubProject: GithubProject   => installFromGithubProject(githubProject, configuration)
        }

      private def installFromLocalDirectory(localDirectory: LocalDirectory): Either[CmtError, String] =
        GenericError(
          s"unable to install course from local directory at '${localDirectory.value.getCanonicalPath}' - installing from a local directory is not supported... yet").asLeft

      private def installFromZipFile(
          zipFile: ZipFile,
          configuration: Configuration,
          deleteZipAfterInstall: Boolean = false): Either[CmtError, String] =
        sbtio.unzip(zipFile.value, configuration.coursesDirectory.value)
        if (deleteZipAfterInstall) {
          sbtio.delete(zipFile.value)
        }
        s"Unzipped '${zipFile.value.name}' to '${configuration.coursesDirectory.value.getAbsolutePath}'".asRight

      private def installFromGithubProject(
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, String] = {
        implicit val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
        val github = Github[IO](httpClient, Some(configuration.githubApiToken.value))
        val latestRelease =
          github.repos.latestRelease(githubProject.organisation, githubProject.project).unsafeToFuture()
        val response = Await.result(latestRelease, 10.seconds)

        response.result match {
          case Left(error) =>
            s"failed to retrieve latest release of ${githubProject.displayName}".toExecuteCommandErrorMessage.asLeft
          case Right(None) =>
            s"failed to retrieve latest release of ${githubProject.displayName}".toExecuteCommandErrorMessage.asLeft
          case Right(Some(release)) =>
            downloadAndInstallStudentifiedRepo(githubProject, release, configuration)
        }
      }

      private def downloadAndInstallStudentifiedRepo(
          githubProject: GithubProject,
          release: Release,
          configuration: Configuration)(implicit client: Client[IO]): Either[CmtError, String] =
        for {
          assetUrl <- getAssetUrl(githubProject, release)
          _ = printMessage(s"downloading studentified course from '$assetUrl' to courses directory")
          downloadedZipFile <- downloadStudentAsset(assetUrl, githubProject, release.tag_name, configuration)
          _ <- installFromZipFile(downloadedZipFile, configuration, deleteZipAfterInstall = true)
          _ <-
            if (cmd.studentifiedAsset.getOrElse(true)) setCurrentCourse(githubProject, configuration)
            else "successfully created new course".toExecuteCommandErrorMessage.asRight
        } yield s"${githubProject.project} (${release.tag_name}) successfully installed to ${configuration.coursesDirectory.value}/${githubProject.project}"

      private def setCurrentCourse(
          githubProject: GithubProject,
          configuration: Configuration): Either[CmtError, String] = {
        val courseDirectory = configuration.coursesDirectory.value / githubProject.project
        val studentifiedRepo = StudentifiedRepo(courseDirectory)
        SetCurrentCourse.Options(studentifiedRepo).execute(configuration)
      }

      private def getAssetUrl(githubProject: GithubProject, release: Release)(implicit
          httpClient: Client[IO]): Either[CmtError, String] = {
        if (cmd.studentifiedAsset.getOrElse(true)) {
          getStudentAssetUrl(githubProject, release)
        } else {
          release.zipball_url.toRight(
            s"latest release of ${githubProject.displayName} does not have a 'main' repo zip - unable to install without one".toExecuteCommandErrorMessage)
        }
      }

      private def getStudentAssetUrl(githubProject: GithubProject, release: Release)(implicit
          httpClient: Client[IO]): Either[CmtError, String] = {
        val maybeAssetsFuture = httpClient
          .expect[List[Asset]](release.assets_url)
          .map { assets =>
            val requiredName =
              if (cmd.studentifiedAsset.getOrElse(true)) s"${githubProject.project}-student.zip"
              else s"${githubProject.project}-${release.tag_name}.zip"
            assets.find(_.name == requiredName).map(_.browserDownloadUrl)
          }
          .unsafeToFuture()
        val maybeStudentAsset = Await.result(maybeAssetsFuture, 10.seconds)
        maybeStudentAsset.toRight(
          s"latest release of ${githubProject.displayName} does not have a studentified zip - unable to install without one".toExecuteCommandErrorMessage)
      }

      private def downloadStudentAsset(
          url: String,
          githubProject: GithubProject,
          tagName: String,
          configuration: Configuration)(implicit client: Client[IO]): Either[CmtError, ZipFile] = {
        val zipFile = ZipFile(configuration.coursesDirectory.value / s"${githubProject.project}.zip")
        downloadFile(url, zipFile)
        zipFile.asRight
      }

      private def downloadFile(fileUri: String, destination: ZipFile)(implicit client: Client[IO]): Unit =
        val _ = (new URL(fileUri) #> new File(destination.value.getAbsolutePath)).!!

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

object GithubSupport:

  case class User(
      name: Option[String],
      email: Option[String],
      login: Option[String],
      id: Option[Long],
      avatarUrl: Option[String],
      gravatarId: Option[String],
      url: Option[String],
      htmlUrl: Option[String],
      followersUrl: Option[String],
      followingUrl: Option[String],
      gistsUrl: Option[String],
      starredUrl: Option[String],
      subscriptionsUrl: Option[String],
      organizationsUrl: Option[String],
      reposUrl: Option[String],
      eventsUrl: Option[String],
      receivedEventsUrl: Option[String],
      userType: Option[String],
      siteAdmin: Option[Boolean])

  implicit lazy val userDecoder: Decoder[User] = Decoder.instance { cursor =>
    for {
      name <- cursor.downField("name").as[Option[String]]
      email <- cursor.downField("email").as[Option[String]]
      login <- cursor.downField("login").as[Option[String]]
      id <- cursor.downField("id").as[Option[Long]]
      avatarUrl <- cursor.downField("avatar_url").as[Option[String]]
      gravatarId <- cursor.downField("gravatar_id").as[Option[String]]
      url <- cursor.downField("url").as[Option[String]]
      htmlUrl <- cursor.downField("html_url").as[Option[String]]
      followersUrl <- cursor.downField("followers_url").as[Option[String]]
      followingUrl <- cursor.downField("following_url").as[Option[String]]
      gistsUrl <- cursor.downField("gists_url").as[Option[String]]
      starredUrl <- cursor.downField("starred_url").as[Option[String]]
      subscriptionsUrl <- cursor.downField("subscriptions_url").as[Option[String]]
      organizationsUrl <- cursor.downField("organizations_url").as[Option[String]]
      reposUrl <- cursor.downField("repos_url").as[Option[String]]
      eventsUrl <- cursor.downField("events_url").as[Option[String]]
      receivedEventsUrl <- cursor.downField("received_events_url").as[Option[String]]
      userType <- cursor.downField("type").as[Option[String]]
      siteAdmin <- cursor.downField("site_admin").as[Option[Boolean]]
    } yield User(
      name,
      email,
      login,
      id,
      avatarUrl,
      gravatarId,
      url,
      htmlUrl,
      followersUrl,
      followingUrl,
      gistsUrl,
      starredUrl,
      subscriptionsUrl,
      organizationsUrl,
      reposUrl,
      eventsUrl,
      receivedEventsUrl,
      userType,
      siteAdmin)
  }

  case class Asset(
      url: String,
      browserDownloadUrl: String,
      id: Long,
      name: String,
      label: Option[String],
      state: String,
      contentType: String,
      size: Long,
      downloadCount: Long,
      createdAt: ZonedDateTime,
      updatedAt: ZonedDateTime,
      uploader: User)

  implicit lazy val assetDecoder: Decoder[Asset] = Decoder.instance { cursor =>
    for {
      url <- cursor.downField("url").as[String]
      browserDownloadUrl <- cursor.downField("browser_download_url").as[String]
      id <- cursor.downField("id").as[Long]
      name <- cursor.downField("name").as[String]
      label <- cursor.downField("label").as[Option[String]]
      state <- cursor.downField("state").as[String]
      contentType <- cursor.downField("content_type").as[String]
      size <- cursor.downField("size").as[Long]
      downloadCount <- cursor.downField("download_count").as[Long]
      createdAt <- cursor.downField("created_at").as[ZonedDateTime]
      updatedAt <- cursor.downField("updated_at").as[ZonedDateTime]
      uploader <- cursor.downField("uploader").as[User]
    } yield Asset(
      url,
      browserDownloadUrl,
      id,
      name,
      label,
      state,
      contentType,
      size,
      downloadCount,
      createdAt,
      updatedAt,
      uploader)
  }

  implicit val assetEntityDecoder: EntityDecoder[IO, List[Asset]] = jsonOf[IO, List[Asset]]

end GithubSupport

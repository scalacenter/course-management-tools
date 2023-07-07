package com.lunatech.cmt.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs, ValueDescription}
import com.lunatech.cmt.{CmtError, printResult}
import com.lunatech.cmt.admin.Domain.{ConfigurationFile, CourseTemplate}
import com.lunatech.cmt.client.command.{Executable, Install}
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.admin.cli.ArgParsers.{configurationFileArgParser, courseTemplateArgParser}
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.client.cli.CmtcCommand
import sbt.io.IO as sbtio
import sbt.io.syntax.*

import java.io.FileFilter

object New:

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
      def execute(configuration: Configuration): Either[CmtError, String] = {
        // list the contents of the ~/Courses directory, if there's anything already matching the name then get the count so we can append to the name and prevent conflicts
        val existingFilesWithSameName = sbtio.listFiles(
          configuration.coursesDirectory.value,
          new FileFilter {
            override def accept(file: File): Boolean =
              file.name.startsWith(options.template.value.project)
          })
        val discriminator = if (existingFilesWithSameName.size > 0) s"-${existingFilesWithSameName.size}" else ""
        val targetDirectoryName = s"${options.template.value.project}$discriminator"

        // Install the course
        Install
          .Options(options.template.value, newName = Some(targetDirectoryName), studentifiedAsset = Some(false))
          .execute(configuration)
      }

  val command = new CmtcCommand[New.Options] {
    def run(options: New.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute(configuration)).printResult()
  }

end New

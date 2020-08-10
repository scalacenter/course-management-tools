package com.lightbend.coursegentools

import java.io.File

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import sbt.io.{IO => sbtio}

import scala.io.StdIn.readLine
import scala.sys.process.Process
import scala.util.{Failure, Success, Try}

object MainAdmInit {

  case class TemplateMetaData(templateName: String, courseName: String, targetPath: File = new File(".")) {
    val courseRootDir: String =
      targetPath.getPath + File.separatorChar + courseName.replace(" ", "-").toLowerCase()
  }

  def initCourseRepo(initCmdOptions: Option[InitCmdOptions], config: MainSettings)
                    (implicit exitOnFirstError: ExitOnFirstError): Unit = {
    initCmdOptions match {
      case Some(cmd) =>
        if (cmd.listTemplates) {
          getAvailableTemplates(config.apiRoot) match {
            case Success(templates) => printTemplateInfo(templates)
            case Failure(err) => printError(err.getMessage)
          }
        } else {
          resolveTemplateMetadata(cmd, config) match {
            case Left(err) => printError(err)
            case Right(templateMetaData) =>
              createCourseFromTemplate(templateMetaData, config)
          }
        }
      case None => printError("Invalid options")
    }
  }

  private def resolveTemplateMetadata(initCmdOptions: InitCmdOptions,
                                      config: MainSettings): Either[String, TemplateMetaData] = {
    val templateName = initCmdOptions.templateName.getOrElse(readLine(toConsoleCyan("Template name: ")).trim)
    if (templateName.isEmpty)
      return Left("Template name can't be empty")

    val courseName = initCmdOptions.courseName.getOrElse(readLine(toConsoleCyan("Course name: ")).trim)
    if (courseName.isEmpty)
      return Left("Course name can't be empty")

    if (!isValidTemplate(templateName, config.apiRoot))
      return Left("Invalid template name. Use 'mainadm init -l' to get available template names.")

    Right(TemplateMetaData(templateName, courseName, initCmdOptions.target))
  }

  private def isValidTemplate(templateName: String, apiRoot: String): Boolean = {
    getAvailableTemplates(apiRoot) match {
      case Success(templates) => templates.contains(templateName)
      case Failure(_) => false
    }
  }

  private def getAvailableTemplates(apiRoot: String): Try[Seq[String]] = {
    val TemplatesDir = "course-templates"

    Try{
      val response = requests.get(s"$apiRoot/contents/$TemplatesDir")
      val json = ujson.read(response.text())
      json.arr.map(_.obj("name").str).toSeq
    }
  }

  private def printTemplateInfo(templates: Seq[String]): Unit = {
    templates.zipWithIndex.foreach{template =>
      println(s"[${template._2}] ${template._1}")
    }
  }

  private def createCourseFromTemplate(template: TemplateMetaData, config: MainSettings): Unit = {
    val tmpDir: String = sbtio.createTemporaryDirectory.getPath
    val curDir = new File(System.getProperty("user.dir"))
    val initResponse = Process(
      Seq(
        config.helperScript,
        "extractTemplate",
        tmpDir,
        config.remoteRepo,
        template.templateName,
        template.courseRootDir
      ), curDir).!

    if (initResponse == 0) {
      dumpStringToFile(template.courseName, s"${template.courseRootDir}/.courseName")
      dumpStringToFile(cleanUpCourseConfig(template.courseRootDir), s"${template.courseRootDir}/course-management.conf")
      doInitialCommit(new File(template.courseRootDir))
    }
  }

  private def cleanUpCourseConfig(courseRepo: String): String = {
    val configFile = new File(courseRepo, "course-management.conf")
    val config: Config = ConfigFactory.parseFile(configFile)
    val renderOptions = ConfigRenderOptions
      .defaults()
      .setOriginComments(false)
      .setComments(true)
      .setFormatted(true)
      .setJson(false)

    config.withoutPath("studentify.relative-source-folder").root().render(renderOptions)
  }

  private def doInitialCommit(workingDir: File): Unit = {
    import ProcessDSL._
    "git init".toProcessCmd(workingDir)
      .runAndExitIfFailed("Failed to init git repository")

    "git add -A".toProcessCmd(workingDir)
      .runAndExitIfFailed(toConsoleRed(s"Failed to add first exercise files"))

    "git commit -m \"Initial commit\""
      .toProcessCmd(workingDir)
      .runAndExitIfFailed(toConsoleRed(s"Failed to commit exercise files"))
  }

}

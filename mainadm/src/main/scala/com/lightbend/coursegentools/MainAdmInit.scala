package com.lightbend.coursegentools

import java.io.File

import sbt.io.{IO => sbtio}

import scala.io.StdIn.readLine
import scala.sys.process.Process
import scala.util.{Failure, Success, Try}

object MainAdmInit {

  case class TemplateMetaData(templateName: String, courseName: String, targetPath: File = new File("."))

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
          resolveTemplateMetadata(cmd) match {
            case Left(err) => printError(err)
            case Right(inputs) =>
              println(s"Start Init template ${inputs.toString}")
              createCourseFromTemplate(inputs, config)
          }
        }
      case None => printError("Invalid options")
    }
  }

  private def resolveTemplateMetadata(initCmdOptions: InitCmdOptions): Either[String, TemplateMetaData] = {
    val templateName = initCmdOptions.templateName.getOrElse(readLine(toConsoleCyan("Template name: ")).trim)
    if (templateName.isEmpty)
      return Left("Template name can't be empty")

    val courseName = initCmdOptions.courseName.getOrElse(readLine(toConsoleCyan("Course name: ")).trim)
    if (courseName.isEmpty)
      return Left("Course name can't be empty")

    Right(TemplateMetaData(templateName, courseName, initCmdOptions.target))
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
    val tmpDir: File = sbtio.createTemporaryDirectory
    val curDir = new File(System.getProperty("user.dir"))
    val remoteRepo = config.remoteRepo
    Process(
      Seq(
        config.helperScript,
        "extractTemplate",
        tmpDir.getPath,
        remoteRepo,
        template.templateName,
        template.targetPath.getPath,
        template.courseName
      ), curDir).!!
  }

}

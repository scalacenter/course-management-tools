package com.lightbend.coursegentools

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

class MasterSettings(masterRepo: File, optConfigurationFile: Option[String]) {

  import Console._
  private val consoleColors: Set[String] = Set("RESET", "GREEN", "RED", "BLUE", "CYAN", "YELLOW", "WHITE", "BLACK", "MAGENTA")
  private val consoleColorMap: Map[String, String] =
    Map("RESET" -> RESET, "GREEN" -> GREEN, "RED" -> RED, "BLUE" -> BLUE, "CYAN" -> CYAN, "YELLOW" -> YELLOW, "WHITE" -> WHITE, "BLACK" -> BLACK, "MAGENTA" -> MAGENTA)

  private def validateColor(settingKey: String): String = {
    val color = config.getString(settingKey)
    val colorUC = color.toUpperCase

    if (! consoleColors.contains(colorUC)) {
      println(s"Setting $settingKey: unknown color $color ")
      System.exit(-1)
    }
    colorUC
  }

  private val referenceConfig = ConfigFactory.load()

  private val cmdLineConfigFile: Option[File] =
    if (optConfigurationFile.isDefined) {
      val configurationFile = new File(masterRepo, optConfigurationFile.get)
      if (!configurationFile.exists()) {
        implicit val eofe: ExitOnFirstError = ExitOnFirstError(true)
        printError(s"No such file: ${optConfigurationFile.get}")
      }
      Some(configurationFile)
    } else {
      None
    }

  private val defaultConfigFile: Option[File] = {
    val configFile = new File(masterRepo, optConfigurationFile.getOrElse("course-management.conf"))
    if (configFile.exists()) Some(configFile) else None
  }

  private val config = (cmdLineConfigFile, defaultConfigFile) match {
    case (Some(cfg), _) =>
      ConfigFactory.parseFile(cfg).withFallback(referenceConfig)

    case (None, Some(cfg)) =>
      ConfigFactory.parseFile(cfg).withFallback(referenceConfig)

    case _ => referenceConfig
  }

  val testCodeFolders: List[String] = config.getStringList("studentify.test-code-folders").asScala.toList

  val exerciseProjectPrefix: String = config.getString("studentify.exercise-project-prefix")

  val studentifyModeSelect: String = config.getString("studentify.studentify-mode-select")

  val studentifyFilesToCleanUp: List[String] = config.getStringList("studentify.studentify-files-to-clean-up").asScala.toList

  val relativeSourceFolder: String = config.getString("studentify.relative-source-folder")

  val useConfigureForProjects: Boolean = config.getBoolean("studentify.use-configure-for-projects")

  object studentifyModeClassic {
    private val classicModeConfig = config.getConfig("studentify.studentify-mode-classic")
    val studentifiedBaseFolder: String = classicModeConfig.getString("studentified-base-folder")
  }

  val solutionsFolder: String = config.getString("studentify.solution-folder")

  val masterBaseProjectName: String = config.getString("studentify.master-base-project-name")
  val studentifiedProjectName: String = config.getString("studentify.studentified-project-name")

  object Colors {

    val promptManColor: String = validateColor("studentify.console-colors.prompt-man-color")
    val promptCourseNameColor: String = validateColor("studentify.console-colors.prompt-course-name")
    val promptExerciseColor: String = validateColor("studentify.console-colors.prompt-exercise-name-color")
  }

}

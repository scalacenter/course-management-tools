package com.lightbend.coursegentools

import java.io.File

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

class MasterSettings(masterRepo: File) {

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
  private val masterConfigFile = new File(masterRepo, "course-management.conf")

  private val config = if (masterConfigFile.exists()) {
    ConfigFactory.parseFile(masterConfigFile).withFallback(referenceConfig)
  } else referenceConfig

  val testCodeFolders: List[String] = config.getStringList("studentify.test-code-folders").asScala.toList

  val studentifiedBaseFolder: String = config.getString("studentify.studentified-base-folder")

  val relativeSourceFolder: String = config.getString("studentify.relative-source-folder")

  val solutionsFolder: String = config.getString("studentify.solution-folder")

  val studentifiedProjectName: String = config.getString("studentify.studentified-project-name")

  object Colors {

    val promptManColor: String = validateColor("studentify.console-colors.prompt-man-color")
    val promptCourseNameColor: String = validateColor("studentify.console-colors.prompt-course-name")
    val promptExerciseColor: String = validateColor("studentify.console-colors.prompt-exercise-name-color")
  }

}

package com.lightbend.coursegentools

import java.io.File
import collection.JavaConverters._

import com.typesafe.config.ConfigFactory

class MasterSettings(masterRepo: File) {

  private val referenceConfig = ConfigFactory.load()
  private val masterConfigFile = new File(masterRepo, "src/main/resources/application.conf")

  private val config = if (masterConfigFile.exists()) {
    ConfigFactory.parseFile(masterConfigFile).withFallback(referenceConfig)
  } else referenceConfig

  val testCodeFolders: List[String] = config.getStringList("studentify.test-code-folders").asScala.toList

  val studentifiedBaseFolder: String = config.getString("studentify.studentified-base-folder")

}

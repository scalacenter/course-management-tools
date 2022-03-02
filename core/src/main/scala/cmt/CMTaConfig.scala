package cmt

import sbt.io.syntax.*

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

class CMTaConfig(mainRepo: File, configFileOpt: Option[File]):

  private val referenceConfig = ConfigFactory.load().resolve()

  private val configFileDefaultName =
    referenceConfig.getString("cmt.config-file-default-name")

  private val defaultLocalConfigFileName = mainRepo / configFileDefaultName

  private val configFileRelativeToMainRepo =
    if configFileOpt.isAbsolute
    then configFileOpt
    else configFileOpt.map(cfg => new File(mainRepo, cfg.getPath))

  private val configFile: Option[File] =
    (configFileOpt.isFile, configFileRelativeToMainRepo.isFile, defaultLocalConfigFileName.isFile) match
      case (true, _, _) => configFileOpt
      case (_, true, _) => configFileRelativeToMainRepo
      case (_, _, true) => Some(defaultLocalConfigFileName)
      case (false, false, false) =>
        if configFileOpt.isDefined then
          printErrorAndExit(s"Configuration: no such file: ${configFileOpt.getOrElse("")}")
          ???
        else None

  private val config =
    if (configFile.isDefined)
      ConfigFactory.parseFile(configFile.get).withFallback(referenceConfig)
    else
      referenceConfig

  val mainRepoExerciseFolder = config.getString("cmt.main-repo-exercise-folder")
  val testCodeFolders = config.getStringList("cmt.test-code-folders").asScala
  val readMeFiles = config.getStringList("cmt.read-me-files").asScala.toSet
  val studentifiedRepoSolutionsFolder =
    config.getString("cmt.studentified-repo-solutions-folder")
  val studentifiedSavedStatesFolder =
    config.getString("cmt.studentified-saved-states-folder")
  val studentifiedRepoActiveExerciseFolder =
    config.getString("cmt.studentified-repo-active-exercise-folder")
  val linearizedRepoActiveExerciseFolder =
    config.getString("cmt.linearized-repo-active-exercise-folder")
  val cmtStudentifiedConfigFile =
    config.getString("cmt.cmt-studentified-config-file")
  val cmtStudentifiedDontTouch =
    config.getStringList("cmt.cmt-studentified-dont-touch").asScala

  extension (oFile: Option[File])
    def isFile: Boolean = oFile.map(file => file.isFile).getOrElse(false)
    def isAbsolute: Boolean =
      oFile.map(file => file.isAbsolute).getOrElse(false)

end CMTaConfig

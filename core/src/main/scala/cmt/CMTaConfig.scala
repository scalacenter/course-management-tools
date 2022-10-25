package cmt

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

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

  // Static settings for cmt specific folders and files
  // CMT metadata root folder
  val cmtMetadataRootFolder = ".cmt"
  val studentifiedRepoSolutionsFolder = s"$cmtMetadataRootFolder/.cue"
  val studentifiedSavedStatesFolder = s"$studentifiedRepoSolutionsFolder/.savedStates"
  val studentifiedRepoBookmarkFile = s"$cmtMetadataRootFolder/.bookmark"
  val cmtStudentifiedConfigFile = s"$cmtMetadataRootFolder/.cmt-config"
  val testCodeSizeAndChecksums = s"$cmtMetadataRootFolder/.cmt-test-size-checksums"

  val mainRepoExerciseFolder = config.getString("cmt.main-repo-exercise-folder")
  val testCodeFolders = config.getStringList("cmt.test-code-folders").asScala
  val readMeFiles = config.getStringList("cmt.read-me-files").asScala.toSet
  val studentifiedRepoActiveExerciseFolder =
    config.getString("cmt.studentified-repo-active-exercise-folder")
  val linearizedRepoActiveExerciseFolder =
    config.getString("cmt.linearized-repo-active-exercise-folder")
  val cmtStudentifiedDontTouch =
    config.getStringList("cmt.cmt-studentified-dont-touch").asScala

  extension (oFile: Option[File])
    def isFile: Boolean = oFile.map(file => file.isFile).getOrElse(false)
    def isAbsolute: Boolean =
      oFile.map(file => file.isAbsolute).getOrElse(false)

end CMTaConfig

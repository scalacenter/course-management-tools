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

import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.typesafe.config.{Config, ConfigFactory, ConfigValue}

import scala.jdk.CollectionConverters.*
import java.nio.charset.StandardCharsets

class CMTcConfig(studentifiedRepo: File):
  import Helpers.adaptToOSSeparatorChar

  private val cmtConfigFile = studentifiedRepo / ".cmt/.cmt-config"
  if !cmtConfigFile.exists then printErrorAndExit("missing CMT configuration file")

  val cmtSettings: Config = ConfigFactory.parseFile(cmtConfigFile)

  val bookmarkFile: File = studentifiedRepo / cmtSettings.getString("studentified-repo-bookmark-file")

  val exercises: collection.mutable.Seq[String] = cmtSettings.getStringList("exercises").asScala

  val dontTouch: Set[String] =
    cmtSettings.getStringList("cmt-studentified-dont-touch").asScala.toSet.map(adaptToOSSeparatorChar)

  val testCodeFolders: Set[String] =
    cmtSettings.getStringList("test-code-folders").asScala.toSet.map(adaptToOSSeparatorChar)

  val readMeFiles: Set[String] = cmtSettings.getStringList("read-me-files").asScala.toSet.map(adaptToOSSeparatorChar)

  val activeExerciseFolder: File =
    studentifiedRepo / cmtSettings.getString("active-exercise-folder")

  val solutionsFolder: File = studentifiedRepo / cmtSettings.getString("studentified-repo-solutions-folder")

  val studentifiedSavedStatesFolder: File =
    studentifiedRepo / cmtSettings.getString("studentified-saved-states-folder")

  private val firstExercise = exercises.head
  private val lastExercise = exercises.last

  val nextExercise: Map[String, String] = ((lastExercise -> lastExercise) +: exercises.zip(exercises.tail)).to(Map)

  val previousExercise: Map[String, String] =
    ((firstExercise -> firstExercise) +: exercises.tail.zip(exercises)).to(Map)

  private val testCodeMetaDataFile = studentifiedRepo / cmtSettings.getString("test-code-size-and-checksums")

  private val metadataConfig = ConfigFactory.parseFile(testCodeMetaDataFile)

  val testCodeMetaData = exercises
    .map { exercise =>
      val x = metadataConfig.getConfig("testcode-metadata").getObjectList(exercise)
      exercise -> exMetadata(x)
    }
    .to(Map)

  val readmeFilesMetaData = exercises
    .map { exercise =>
      val x = metadataConfig.getConfig("readmefiles-metadata").getObjectList(exercise)
      exercise -> exMetadata(x)
    }
    .to(Map)

end CMTcConfig

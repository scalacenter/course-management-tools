package cmt.client

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

import cmt.printErrorAndExit
import cmt.Helpers.adaptToOSSeparatorChar
import com.typesafe.config.{Config, ConfigFactory}
import sbt.io.syntax.*
import scala.jdk.CollectionConverters.*

object Domain:
  final case class ExerciseId(value: String)
  object ExerciseId:
    val default: ExerciseId = ExerciseId("")

  final case class StudentifiedRepo(value: File) {
    if !cmtConfigFile.exists then printErrorAndExit("missing CMT configuration file")

    private lazy val cmtConfigFile = value / ".cmt-config"

    lazy val bookmarkFile: File = value / ".bookmark"

    lazy val cmtSettings: Config = ConfigFactory.parseFile(cmtConfigFile)

    lazy val exercises: collection.mutable.Seq[String] = cmtSettings.getStringList("exercises").asScala

    lazy val dontTouch: Set[String] =
      cmtSettings.getStringList("cmt-studentified-dont-touch").asScala.toSet.map(adaptToOSSeparatorChar)

    lazy val testCodeFolders: Set[String] =
      cmtSettings.getStringList("test-code-folders").asScala.toSet.map(adaptToOSSeparatorChar)

    lazy val readMeFiles: Set[String] =
      cmtSettings.getStringList("read-me-files").asScala.toSet.map(adaptToOSSeparatorChar)

    lazy val activeExerciseFolder: File =
      value / cmtSettings.getString("active-exercise-folder")

    lazy val solutionsFolder: File = value / cmtSettings.getString("studentified-repo-solutions-folder")
    lazy val studentifiedSavedStatesFolder: File =
      solutionsFolder / cmtSettings.getString("studentified-saved-states-folder")

    lazy val nextExercise: Map[String, String] = exercises.zip(exercises.tail).to(Map)

    lazy val previousExercise: Map[String, String] =
      exercises.tail.zip(exercises).to(Map)
  }
  object StudentifiedRepo:
    val default: StudentifiedRepo = StudentifiedRepo(file(".").getAbsoluteFile.getParentFile)

  final case class TemplatePath(value: String)
  object TemplatePath:
    val default: TemplatePath = TemplatePath("")

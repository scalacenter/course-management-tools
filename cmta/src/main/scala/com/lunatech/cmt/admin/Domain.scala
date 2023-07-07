package com.lunatech.cmt.admin

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

import com.lunatech.cmt.Domain.InstallationSource.GithubProject
import sbt.io.syntax.File

object Domain:

  final case class RenumberStart(value: Int)

  final case class RenumberOffset(value: Int)
  object RenumberOffset:
    val default: RenumberOffset = RenumberOffset(1)

  final case class RenumberStep(value: Int)
  object RenumberStep:
    val default: RenumberStep = RenumberStep(1)

  final case class ExerciseNumber(value: Int)
  object ExerciseNumber:
    val default: ExerciseNumber = ExerciseNumber(0)

  final case class StudentifyBaseDirectory(value: File)
  final case class ForceDeleteDestinationDirectory(value: Boolean)
  final case class InitializeGitRepo(value: Boolean)
  final case class LinearizeBaseDirectory(value: File)
  final case class MainRepository(value: File)
  final case class ConfigurationFile(value: File)

  final case class CourseTemplate(value: GithubProject)
  object CourseTemplate:
    def fromString(str: String): CourseTemplate =
      if (str.indexOf("/") > 0) {
        val Array(organisation, project) = str.split("/").map(_.trim)
        CourseTemplate(GithubProject(organisation, project))
      } else {
        CourseTemplate(GithubProject("lunatech-labs", str))
      }

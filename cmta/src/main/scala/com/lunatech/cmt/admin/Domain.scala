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

import com.lunatech.cmt.CmtError
import sbt.io.syntax.File
import com.lunatech.cmt.*
import cats.syntax.either.*

import com.lunatech.cmt.Domain.InstallationSource

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

  final case class CourseTemplate(value: Either[CmtError, InstallationSource.GithubProject])
  object CourseTemplate:
    val GithubTemplateRegex = "([A-Za-z0-9-_]*)".r
    val GithubProjectRegex = "([A-Za-z0-9-_]*)\\/([A-Za-z0-9-_]*)".r
    val GithubProjectWithTagRegex = "([A-Za-z0-9-_]*)\\/([A-Za-z0-9-_]*)\\/(.*)".r
    def fromString(str: String): CourseTemplate =
      str match {
        case GithubTemplateRegex(template) =>
          CourseTemplate(Right(InstallationSource.GithubProject("lunatech-labs", s"cmt-template-$template", None)))
        case GithubProjectRegex(organisation, project) =>
          CourseTemplate(Right(InstallationSource.GithubProject(organisation, project, None)))
        case GithubProjectWithTagRegex(organisation, project, tag) =>
          CourseTemplate(Right(InstallationSource.GithubProject(organisation, project, Some(tag))))
        case _ => CourseTemplate(s"Invalid template name: $str".toExecuteCommandErrorMessage.asLeft)
      }

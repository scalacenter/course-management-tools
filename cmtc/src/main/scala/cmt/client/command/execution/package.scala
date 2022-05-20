package cmt.client.command

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

import cmt.Helpers.writeStudentifiedCMTBookmark
import cmt.client.Domain.StudentifiedRepo
import cmt.{CMTcConfig, Helpers}
import sbt.io.syntax.File
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile

package object execution {

  private final case class PathARO(absolutePath: File, maybeRelativePath: Option[File])

  private final case class PathAR(absolutePath: File, relativePath: File)

  def getCurrentExerciseState(studentifiedRepo: StudentifiedRepo): Seq[File] =
    Helpers
      .fileList(studentifiedRepo.activeExerciseFolder)
      .map(fileAbsolute => PathARO(fileAbsolute, fileAbsolute.relativeTo(studentifiedRepo.value)))
      .collect { case PathARO(fileAbsolute, Some(fileRelative)) =>
        PathAR(fileAbsolute, fileRelative)
      }
      .filterNot { case PathAR(_, fileRelative) =>
        studentifiedRepo.dontTouch.exists(lead => fileRelative.getPath.startsWith(lead))
      }
      .map { _.absolutePath }

  def deleteCurrentState(studentifiedRepo: StudentifiedRepo): Unit =
    val filesToBeDeleted: Seq[File] = getCurrentExerciseState(studentifiedRepo)
    sbtio.deleteFilesEmptyDirs(filesToBeDeleted)

  def copyTestCodeAndReadMeFiles(studentifiedRepo: StudentifiedRepo, solution: File, prevOrNextExercise: String): Unit =
    for {
      testCodeFolder <- studentifiedRepo.testCodeFolders
      fromFolder = solution / testCodeFolder
      toFolder = studentifiedRepo.activeExerciseFolder / testCodeFolder
    } {
      sbtio.delete(toFolder)
      sbtio.copyDirectory(fromFolder, toFolder)
    }
    for {
      readmeFile <- studentifiedRepo.readMeFiles if (solution / readmeFile).exists
    } sbtio.copyFile(solution / readmeFile, studentifiedRepo.activeExerciseFolder / readmeFile)

    writeStudentifiedCMTBookmark(studentifiedRepo.bookmarkFile, prevOrNextExercise)

  def starCurrentExercise(currentExercise: String, exercise: String): String =
    if (currentExercise == exercise) " * " else "   "
}

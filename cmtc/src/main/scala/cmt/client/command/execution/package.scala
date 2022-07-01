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
import cmt.{CMTcConfig, Helpers}
import sbt.io.syntax.File
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile

import java.nio.charset.StandardCharsets

package object execution {

  private final case class PathARO(absolutePath: File, maybeRelativePath: Option[File])

  private final case class PathAR(absolutePath: File, relativePath: File)

  def getCurrentExerciseState(studentifiedRepo: File)(config: CMTcConfig): Seq[File] =
    Helpers
      .fileList(config.activeExerciseFolder)
      .map(fileAbsolute => PathARO(fileAbsolute, fileAbsolute.relativeTo(studentifiedRepo)))
      .collect { case PathARO(fileAbsolute, Some(fileRelative)) =>
        PathAR(fileAbsolute, fileRelative)
      }
      .filterNot { case PathAR(_, fileRelative) =>
        config.dontTouch.exists(lead => fileRelative.getPath.startsWith(lead))
      }
      .map { _.absolutePath }

  def deleteCurrentState(studentifiedRepo: File)(config: CMTcConfig): Unit =
    val filesToBeDeleted: Seq[File] = getCurrentExerciseState(studentifiedRepo)(config)
    sbtio.deleteFilesEmptyDirs(filesToBeDeleted)

  def getCurrentExerciseId(bookmarkFile: File): String =
    sbtio.readLines(bookmarkFile, StandardCharsets.UTF_8).head

  def copyTestCodeAndReadMeFiles(solution: File, prevOrNextExercise: String)(config: CMTcConfig): Unit =

    val (pathsToCopy, redundantPaths) =
      Helpers.extractUniquePaths(config.testCodeFolders.to(List) ++ config.readMeFiles.to(List))

    for {
      path <- pathsToCopy
    } sbtio.delete(config.activeExerciseFolder / path)

    val (dirs, files) =
      pathsToCopy
        .filter { path =>
          val solutionPath = solution / path
          solution.exists()
        }
        .partition(path => (solution / path).isDirectory)

    for {
      dir <- dirs
      fromFolder = solution / dir
      toFolder = config.activeExerciseFolder / dir
    } {
      sbtio.copyDirectory(fromFolder, toFolder)
    }
    for {
      file <- files if (solution / file).exists
    } sbtio.copyFile(solution / file, config.activeExerciseFolder / file)

    writeStudentifiedCMTBookmark(config.bookmarkFile, prevOrNextExercise)

  def starCurrentExercise(currentExercise: String, exercise: String): String =
    if (currentExercise == exercise) " * " else "   "
}

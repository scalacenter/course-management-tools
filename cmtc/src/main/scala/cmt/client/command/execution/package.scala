package cmt.client.command

import cmt.Helpers.writeStudentifiedCMTBookmark
import cmt.{CMTcConfig, Helpers}
import sbt.io.syntax.File
import sbt.io.IO as sbtio
import sbt.io.syntax.fileToRichFile

package object execution {

  private final case class PathARO(absolutePath: File, maybeRelativePath: Option[File])

  private final case class PathAR(absolutePath: File, relativePath: File)

  def deleteCurrentState(studentifiedRepo: File)(config: CMTcConfig): Unit =
    val filesToBeDeleted =
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

    sbtio.deleteFilesEmptyDirs(filesToBeDeleted)

  def copyTestCodeAndReadMeFiles(solution: File, prevOrNextExercise: String)(config: CMTcConfig): Unit =
    for {
      testCodeFolder <- config.testCodeFolders
      fromFolder = solution / testCodeFolder
      toFolder = config.activeExerciseFolder / testCodeFolder
    } {
      sbtio.delete(toFolder)
      sbtio.copyDirectory(fromFolder, toFolder)
    }
    for {
      readmeFile <- config.readMeFiles
    } sbtio.copyFile(solution / readmeFile, config.activeExerciseFolder / readmeFile)

    writeStudentifiedCMTBookmark(config.bookmarkFile, prevOrNextExercise)

  def starCurrentExercise(currentExercise: String, exercise: String): String =
    if (currentExercise == exercise) " * " else "   "
}

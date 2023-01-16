package cmt.client

import cmt.{CMTcConfig, Helpers}
import cmt.Helpers.writeStudentifiedCMTBookmark
import sbt.io.syntax.*
import sbt.io.IO as sbtio
import java.nio.charset.StandardCharsets

package object command:

  final case class ExerciseFiles(filesAbsolute: Seq[File], filesRelative: Seq[File])
  private final case class PathARO(absolutePath: File, maybeRelativePath: Option[File])

  private final case class PathAR(absolutePath: File, relativePath: File)

  def getCurrentExerciseStateExceptDontTouch(studentifiedRepo: File)(config: CMTcConfig): ExerciseFiles =
    val currentStateFilesExceptDontTouchAbsolute = Helpers
      .fileList(config.activeExerciseFolder)
      .map(fileAbsolute => PathARO(fileAbsolute, fileAbsolute.relativeTo(studentifiedRepo)))
      .collect { case PathARO(fileAbsolute, Some(fileRelative)) =>
        PathAR(fileAbsolute, fileRelative)
      }
      .filterNot { case PathAR(_, fileRelative) =>
        config.dontTouch.exists(lead => fileRelative.getPath.startsWith(lead))
      }
      .map { x => x.absolutePath }
    val currentStateFilesExceptDontTouchRelative =
      currentStateFilesExceptDontTouchAbsolute.map(_.relativeTo(config.activeExerciseFolder)).collect { case Some(x) =>
        x
      }
    ExerciseFiles(
      filesAbsolute = currentStateFilesExceptDontTouchAbsolute,
      filesRelative = currentStateFilesExceptDontTouchRelative)

  def deleteCurrentState(studentifiedRepo: File)(config: CMTcConfig): Unit =
    val ExerciseFiles(filesToBeDeleted, _) = getCurrentExerciseStateExceptDontTouch(studentifiedRepo)(config)
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
      pathsToCopy.filter(path => (solution / path).exists()).partition(path => (solution / path).isDirectory)

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

end command

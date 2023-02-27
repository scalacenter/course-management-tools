package cmt.client

import cmt.client.command.{getCurrentExerciseId, starCurrentExercise}
import cmt.{CMTcConfig, CmtError, ErrorMessage, FailedToValidateArgument, OptionName, toConsoleGreen}
import sbt.io.syntax.*
extension (f: File)
  // Gets the parent folder of this folder but return this
  // folder if it's a root folder
  private def getParentSafe: File =
    val pf = f.getParentFile()
    if (pf == null) f else pf

private val cmtSignature1 = ".cmt/.cmt-config"
private val cmtSignature2 = ".cmt/.bookmark"

private def isStudentifiedRepo(folder: File): Boolean =
  (folder / cmtSignature1).exists && (folder / cmtSignature2).exists

/** @param Path
  *   to either the root of a studentified repo or any subfolder in such repo
  * @return
  *   The root folder of the studentified repo or an error message in case the passed-in fodler wasn't pointing to a
  *   studentified repo.
  */
def findStudentRepoRoot(path: File): Either[CmtError, File] =
  lazy val error = FailedToValidateArgument.because("s", s"$path is not a CMT student project")
  @scala.annotation.tailrec
  def findStudentRepoRootRecurse(path: File): Option[File] =
    if (path.isDirectory() && isStudentifiedRepo(path)) Some(path)
    else
      val pf = path.getParentSafe
      if (path == pf)
        None
      else
        findStudentRepoRootRecurse(pf)

  if (path.isDirectory())
    findStudentRepoRootRecurse(path).toRight(error)
  else
    Left(error)

def listExercises(config: CMTcConfig): String =
  val currentExerciseId = getCurrentExerciseId(config.bookmarkFile)

  config.exercises.zipWithIndex
    .map { case (exName, index) =>
      toConsoleGreen(f"${index + 1}%3d. ${starCurrentExercise(currentExerciseId, exName)}  $exName")
    }
    .mkString("\n")

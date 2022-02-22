package cmt

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

import java.nio.charset.StandardCharsets

import sbt.io.syntax.*
import sbt.io.{IO as sbtio}

import Helpers.*

object CMTStudent:
  def moveToNextExercise(studentifiedRepo: File)(config: CMTcConfig): Unit =

    val currentExercise =
      sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head

    if (currentExercise == config.exercises.last) then
      println(
        toConsoleGreen(s"You're already at the last exercise: $currentExercise")
      )
    else
      withZipFile(
        config.solutionsFolder,
        config.nextExercise(currentExercise)
      ) { solution =>
        copyTestCodeAndReadMeFiles(
          solution,
          config.nextExercise(currentExercise),
          s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${config.nextExercise(currentExercise)}")}"
        )(config)
      }
  end moveToNextExercise

  def moveToPreviousExercise(studentifiedRepo: File)(config: CMTcConfig): Unit =

    val currentExercise =
      sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head

    if (currentExercise == config.exercises.head) then
      println(
        toConsoleGreen(
          s"You're already at the first exercise: $currentExercise"
        )
      )
    else
      withZipFile(
        config.solutionsFolder,
        config.previousExercise(currentExercise)
      ) { solution =>
        println(
          s"Current: $currentExercise - Previous: ${config.previousExercise(currentExercise)}"
        )
        copyTestCodeAndReadMeFiles(
          solution,
          config.previousExercise(currentExercise),
          s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${config.previousExercise(currentExercise)}")}"
        )(config)
      }
  end moveToPreviousExercise

  def pullTemplate(studentifiedRepo: File, templatePath: String)(
      config: CMTcConfig
  ): Unit =
    import sbt.io.CopyOptions
    val currentExercise =
      sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head

    withZipFile(
      config.solutionsFolder,
      currentExercise
    ) { solution =>
      val fullTemplatePath = solution / templatePath
      if fullTemplatePath.exists then
        sbtio.copyFile(fullTemplatePath, config.activeExerciseFolder / templatePath, CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
      else printError(s"No such template: $templatePath")
    }


  def gotoExercise(studentifiedRepo: File, exercise: String)(
      config: CMTcConfig
  ): Unit =

    if !config.exercises.contains(exercise) then
      printError(s"No such exercise: $exercise")
    
    withZipFile(
      config.solutionsFolder,
      exercise
    ) { solution =>
      copyTestCodeAndReadMeFiles(
        solution,
        exercise,
        s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"${exercise}")}"
      )(config)
    }

    Helpers.writeStudentifiedCMTBookmark(config.bookmarkFile, exercise)

  end gotoExercise

  def copyTestCodeAndReadMeFiles(
      solution: File,
      prevOrNextExercise: String,
      message: String
  )(config: CMTcConfig): Unit =
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
    } sbtio.copyFile(
      solution / readmeFile,
      config.activeExerciseFolder / readmeFile
    )

    writeStudentifiedCMTBookmark(config.bookmarkFile, prevOrNextExercise)
    println(message)
  end copyTestCodeAndReadMeFiles

  def listExercises(studentifiedRepo: File)(config: CMTcConfig): Unit =

    val currentExercise =
      sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head
    config.exercises.zipWithIndex
      .foreach { case (exName, index) =>
        println(
          toConsoleGreen(
            f"${index + 1}%3d. ${starCurrentExercise(currentExercise, exName)}  $exName"
          )
        )
      }
  end listExercises

  def pullSolution(studentifiedRepo: File)(
      config: CMTcConfig
  ): Unit =

    val currentExercise =
      sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head

    deleteCurrentState()(config)

    Helpers.withZipFile(config.solutionsFolder, currentExercise) { solution =>
      val files = Helpers.fileList(solution / currentExercise)
      sbtio.copyDirectory(
        config.solutionsFolder / currentExercise,
        config.activeExerciseFolder,
        preserveLastModified = true
      )
    }

    println(toConsoleGreen(s"Pulled solution for $currentExercise"))
  end pullSolution

  def restoreState(studentifiedRepo: File, exercise: String)(
      config: CMTcConfig
  ): Unit =
    val savedState = config.studentifiedSavedStatesFolder / s"${exercise}.zip"
    if !savedState.exists then printError(s"No such saved state: $exercise")

    deleteCurrentState()(config)

    Helpers.withZipFile(config.studentifiedSavedStatesFolder, exercise) {
      solution =>
        val files = Helpers.fileList(solution / exercise)
        sbtio.copyDirectory(
          config.studentifiedSavedStatesFolder / exercise,
          config.activeExerciseFolder,
          preserveLastModified = true
        )
    }

    Helpers.writeStudentifiedCMTBookmark(config.bookmarkFile, exercise)

    println(toConsoleGreen(s"Restored state for $exercise"))
  end restoreState

  def saveState(studentifiedRepo: File)(
      config: CMTcConfig
  ): Unit =
    val currentExercise =
      sbtio.readLines(config.bookmarkFile, StandardCharsets.UTF_8).head
    val savedStatesFolder = config.studentifiedSavedStatesFolder
    sbtio.delete(savedStatesFolder / currentExercise)
    sbtio.copyDirectory(
      config.activeExerciseFolder,
      savedStatesFolder / currentExercise
    )

    zipAndDeleteOriginal(
      baseFolder = savedStatesFolder,
      zipToFolder = savedStatesFolder,
      exercise = currentExercise
    )

    println(toConsoleGreen(s"Saved state for $currentExercise"))
  end saveState

  def listSavedStates(studentifiedRepo: File)(config: CMTcConfig): Unit =
    val MatchDotzip = ".zip".r
    val savedStates =
      sbtio
        .listFiles(config.studentifiedSavedStatesFolder)
        .to(List)
        .sorted
        .map(_.getName)
        .map(item => MatchDotzip.replaceAllIn(item, ""))
        .filter(config.exercises.contains(_))

    println(
      toConsoleGreen(
        s"Saved states available for exercises:\n"
      ) + toConsoleYellow(s"${savedStates.mkString("\n   ", "\n   ", "\n")}")
    )
  end listSavedStates

  def validateStudentifiedRepo(studentifiedRepo: File)(using
      config: CMTcConfig
  ): Unit =
    // TODO
    ()

  def starCurrentExercise(currentExercise: String, exercise: String): String = {
    if (currentExercise == exercise) " * " else "   "
  }

  private final case class PathARO(
      absolutePath: File,
      maybeRelativePath: Option[File]
  )

  private final case class PathAR(absolutePath: File, relativePath: File)

  private def deleteCurrentState()(config: CMTcConfig): Unit =
    val filesToBeDeleted =
      Helpers
        .fileList(config.activeExerciseFolder)
        .map(fileAbsolute =>
          PathARO(
            fileAbsolute,
            fileAbsolute.relativeTo(config.activeExerciseFolder)
          )
        )
        .collect { case PathARO(fileAbsolute, Some(fileRelative)) =>
          PathAR(fileAbsolute, fileRelative)
        }
        .filterNot { case PathAR(_, fileRelative) =>
          config.dontTouch.exists(lead => fileRelative.getPath.startsWith(lead))
        }
        .map { _.absolutePath }
    sbtio.delete(filesToBeDeleted)
  end deleteCurrentState

end CMTStudent

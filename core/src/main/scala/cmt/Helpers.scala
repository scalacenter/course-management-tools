package cmt

import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}

import scala.jdk.CollectionConverters.*

final case class StudentifiedSkelFolders(solutionsFolder: File)
object Helpers:

  def fileList(base: File): Vector[File] =
    @scala.annotation.tailrec
    def fileList(filesSoFar: Vector[File], folders: Vector[File]): Vector[File] =
      val subs = (folders.foldLeft(Vector.empty[File])) { case (tally, folder) =>
        tally ++ sbtio.listFiles(folder)
      }
      subs.partition(_.isDirectory) match
        case (rem, result) if rem.isEmpty => filesSoFar ++ result
        case (rem, tally)                 => fileList(filesSoFar ++ tally, rem)

    val (seedFolders, seedFiles) =
      sbtio.listFiles(base).partition(_.isDirectory)
    fileList(seedFiles.toVector, seedFolders.toVector)
  end fileList

  def resolveMainRepoPath(mainRepo: File): Either[String, File] = {
    for {
      rp <- getRepoPathFromGit(mainRepo)
    } yield new File(rp)
  }

  private def getRepoPathFromGit(repo: File): Either[String, String] = {
    import ProcessDSL.*
    "git rev-parse --show-toplevel".toProcessCmd(workingDir = repo).runAndReadOutput()
  }

  def exitIfGitIndexOrWorkspaceIsntClean(mainRepo: File): Unit =
    import ProcessDSL.toProcessCmd
    val workspaceIsUnclean = "git status --porcelain"
      .toProcessCmd(workingDir = mainRepo)
      .runAndReadOutput()
      .map(str => str.split("\n").toSeq.map(_.trim).filter(_ != ""))
      .map(_.length)

    workspaceIsUnclean match {
      case Right(cnt) if cnt > 0 =>
        printErrorAndExit(s"main repository isn't clean. Commit changes and try again")
      case Right(_) => ()
      case Left(_)  =>
    }

  def createStudentifiedFolderSkeleton(stuBase: File, studentifiedRootFolder: File)(config: CMTaConfig) =
    if studentifiedRootFolder.exists then printErrorAndExit(s"$studentifiedRootFolder exists already")
    if !stuBase.canWrite then printErrorAndExit(s"$stuBase isn't writeable")

    val solutionsFolder =
      studentifiedRootFolder / config.studentifiedRepoSolutionsFolder

    sbtio.createDirectories(Seq(studentifiedRootFolder, solutionsFolder))
    StudentifiedSkelFolders(solutionsFolder)

  def addFirstExercise(cleanedMainRepo: File, firstExercise: String, studentifiedRootFolder: File)(config: CMTaConfig) =
    sbtio.copyDirectory(
      cleanedMainRepo / config.mainRepoExerciseFolder / firstExercise,
      studentifiedRootFolder / config.studentifiedRepoActiveExerciseFolder)

  final case class ExercisePrefixesAndExerciseNames(prefixes: Set[String], exercises: Vector[String])

  def getExercisePrefixAndExercises(mainRepo: File)(config: CMTaConfig): ExercisePrefixesAndExerciseNames =
    val PrefixSpec = raw"(.*)_\d{3}_\w+$$".r
    val matchedNames =
      sbtio.listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder).map(_.getName).to(List)
    val prefixes = matchedNames.map { case PrefixSpec(n) => n }.to(Set)
    val exercises = sbtio
      .listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder)
      .map(_.getName)
      .to(Vector)
      .sorted match
      case Vector() =>
        printErrorAndExit("No exercises found. Check your configuration"); ???
      case exercises => exercises
    ExercisePrefixesAndExerciseNames(prefixes, exercises)
  end getExercisePrefixAndExercises

  def validatePrefixes(prefixes: Set[String]): Unit =
    if prefixes.size > 1 then printErrorAndExit(s"Multiple exercise prefixes (${prefixes.mkString(", ")}) found")

  def zipAndDeleteOriginal(baseFolder: File, zipToFolder: File, exercise: String, time: Option[Long] = None): Unit =
    val filesToZip = fileList(baseFolder / exercise).map(f => (f, sbtio.relativize(baseFolder, f))).collect {
      case (f, Some(s)) => (f, s)
    }
    val zipFile = zipToFolder / s"${exercise}.zip"
    sbtio.zip(filesToZip, zipFile, time)
    sbtio.delete(baseFolder / exercise)
  end zipAndDeleteOriginal

  def hideExercises(cleanedMainRepo: File, solutionsFolder: File, exercises: Vector[String])(config: CMTaConfig): Unit =
    val now: Option[Long] = Some(java.time.Instant.now().toEpochMilli())
    for (exercise <- exercises)
      zipAndDeleteOriginal(cleanedMainRepo / config.mainRepoExerciseFolder, solutionsFolder, exercise, now)
  end hideExercises

  def dumpStringToFile(string: String, file: File): Unit =
    import java.nio.charset.StandardCharsets
    import java.nio.file.Files
    Files.write(file.toPath, string.getBytes(StandardCharsets.UTF_8))

  def writeStudentifiedCMTConfig(configFile: File, exercises: Seq[String])(config: CMTaConfig): Unit =
    val configMap = Map(
      "studentified-repo-solutions-folder" -> config.studentifiedRepoSolutionsFolder,
      "studentified-saved-states-folder" -> config.studentifiedSavedStatesFolder,
      "active-exercise-folder" -> config.studentifiedRepoActiveExerciseFolder,
      "test-code-folders" -> config.testCodeFolders.asJava,
      "read-me-files" -> config.readMeFiles.asJava,
      "exercises" -> exercises.asJava,
      "cmt-studentified-dont-touch" -> config.cmtStudentifiedDontTouch
        .map(path => s"${config.studentifiedRepoActiveExerciseFolder}/${path}")
        .asJava)
    val cmtConfig =
      ConfigFactory.parseMap(configMap.asJava).root().render(ConfigRenderOptions.concise().setFormatted(true))
    dumpStringToFile(cmtConfig, configFile)

  def writeStudentifiedCMTBookmark(bookmarkFile: File, firstExercise: String): Unit =
    dumpStringToFile(firstExercise, bookmarkFile)

  def withZipFile(solutionsFolder: File, exerciseID: String)(code: File => Any): Unit =
    val archive = solutionsFolder / s"$exerciseID.zip"
    sbtio.unzip(archive, solutionsFolder)
    code(solutionsFolder / exerciseID)
    sbtio.delete(solutionsFolder / exerciseID)
  end withZipFile

  def initializeGitRepo(linearizedProject: File): Unit =
    import ProcessDSL.toProcessCmd
    s"git init"
      .toProcessCmd(workingDir = linearizedProject)
      .runAndExitIfFailed(
        toConsoleRed(s"Failed to initialize linearized git repository in ${linearizedProject.getPath}"))
  end initializeGitRepo

  def commitToGit(commitMessage: String, projectFolder: File): Unit =
    import ProcessDSL.toProcessCmd
    s"git add -A"
      .toProcessCmd(workingDir = projectFolder)
      .runAndExitIfFailed(toConsoleRed(s"Failed to add first exercise files"))
    s"""git commit -m "$commitMessage""""
      .toProcessCmd(workingDir = projectFolder)
      .runAndExitIfFailed(toConsoleRed(s"Failed to commit files for $commitMessage"))
  end commitToGit

  private val ExerciseNumberSpec = raw".*_(\d{3})_.*".r

  def extractExerciseNr(exercise: String): Int = {
    val ExerciseNumberSpec(d) = exercise: @unchecked
    d.toInt
  }

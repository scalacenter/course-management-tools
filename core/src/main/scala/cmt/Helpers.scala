package cmt

import cmt.ProcessDSL.ProcessCmd
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

  def exitIfGitIndexOrWorkspaceIsntClean(mainRepo: File): Either[String, Unit] =
    import ProcessDSL.toProcessCmd
    val workspaceIsUnclean = "git status --porcelain"
      .toProcessCmd(workingDir = mainRepo)
      .runAndReadOutput()
      .map(str => str.split("\n").toSeq.map(_.trim).filter(_ != ""))
      .map(_.length)

    workspaceIsUnclean match {
      case Right(cnt) if cnt > 0 =>
        Left(s"main repository isn't clean. Commit changes and try again")
      case Right(_)  => Right(())
      case Left(msg) => Left(msg)
    }

  def createStudentifiedFolderSkeleton(stuBase: File, studentifiedRootFolder: File)(
      config: CMTaConfig): StudentifiedSkelFolders =
    if studentifiedRootFolder.exists then printErrorAndExit(s"$studentifiedRootFolder exists already")
    if !stuBase.canWrite then printErrorAndExit(s"$stuBase isn't writeable")

    val solutionsFolder =
      studentifiedRootFolder / config.studentifiedRepoSolutionsFolder

    sbtio.createDirectories(Seq(studentifiedRootFolder, solutionsFolder))
    StudentifiedSkelFolders(solutionsFolder)
  end createStudentifiedFolderSkeleton

  def addFirstExercise(cleanedMainRepo: File, firstExercise: String, studentifiedRootFolder: File)(
      config: CMTaConfig): Unit =
    sbtio.copyDirectory(
      cleanedMainRepo / config.mainRepoExerciseFolder / firstExercise,
      studentifiedRootFolder / config.studentifiedRepoActiveExerciseFolder)
  end addFirstExercise

  final case class ExercisesMetadata(exercisePrefix: String, exercises: Vector[String], exerciseNumbers: Vector[Int])

  def getExerciseMetadata(mainRepo: File)(config: CMTaConfig): Either[String, ExercisesMetadata] =
    val PrefixSpec = raw"(.*)_\d{3}_\w+$$".r
    val matchedNames =
      sbtio.listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder).map(_.getName).to(List)
    val prefixes = matchedNames.map { case PrefixSpec(n) => n }.to(Set)
    sbtio.listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder).map(_.getName).to(Vector).sorted match
      case Vector() =>
        Left("No exercises found. Check your configuration")
      case exercises =>
        prefixes.size match
          case 0 => Left("No exercises found")
          case 1 =>
            val exerciseNumbers = exercises.map(extractExerciseNr)
            if exerciseNumbers.size == exerciseNumbers.to(Set).size then
              Right(ExercisesMetadata(prefixes.head, exercises, exerciseNumbers))
            else Left("Duplicate exercise numbers found")
          case _ => Left(s"Multiple exercise prefixes (${prefixes.mkString(", ")}) found")
  end getExerciseMetadata

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

  def withZipFile(solutionsFolder: File, exerciseID: String)(
      code: File => Either[String, String]): Either[String, String] =
    val archive = solutionsFolder / s"$exerciseID.zip"
    sbtio.unzip(archive, solutionsFolder)
    val retVal = code(solutionsFolder / exerciseID)
    sbtio.delete(solutionsFolder / exerciseID)
    retVal
  end withZipFile

  def initializeGitRepo(linearizedProject: File): Either[String, Unit] =
    import ProcessDSL.toProcessCmd
    s"git init"
      .toProcessCmd(workingDir = linearizedProject)
      .runWithStatus(toConsoleRed(s"Failed to initialize linearized git repository in ${linearizedProject.getPath}"))
  end initializeGitRepo

  def commitToGit(commitMessage: String, projectFolder: File): Either[String, Unit] =
    import ProcessDSL.toProcessCmd

    for {
      _ <- s"git add -A"
        .toProcessCmd(workingDir = projectFolder)
        .runWithStatus(toConsoleRed(s"Failed to add first exercise files"))
      result <- s"""git commit -m "$commitMessage""""
        .toProcessCmd(workingDir = projectFolder)
        .runWithStatus(toConsoleRed(s"Failed to commit files for $commitMessage"))
    } yield result
  end commitToGit

  private val ExerciseNumberSpec = raw".*_(\d{3})_.*".r

  def extractExerciseNr(exercise: String): Int = {
    val ExerciseNumberSpec(d) = exercise: @unchecked
    d.toInt
  }

  def copyCleanViaGit(mainRepo: File, tmpDir: File, repoName: String): Either[String, Unit] =

    import ProcessDSL.*

    import java.util.UUID
    val initBranch = UUID.randomUUID.toString
    val tmpRemoteBranch = s"CMT-${UUID.randomUUID.toString}"
    val script = List(
      (s"${tmpDir.getPath}", List(s"mkdir ${repoName}.git", s"git init --bare ${repoName}.git")),
      (
        s"${mainRepo.getPath}",
        List(
          s"git remote add ${tmpRemoteBranch} ${tmpDir.getPath}/${repoName}.git",
          s"git push ${tmpRemoteBranch} HEAD:refs/heads/${initBranch}")),
      (
        s"${tmpDir.getPath}",
        List(
          s"git clone -b ${initBranch} ${tmpDir.getPath}/${repoName}.git",
          s"rm -rf ${tmpDir.getPath}/${repoName}.git")),
      (s"${mainRepo.getPath}", List(s"git remote remove ${tmpRemoteBranch}")))
    val commands = for {
      (workingDir, commands) <- script
      command <- commands
    } yield command.toProcessCmd(new File(workingDir))

    for {
      result <- runCommands(commands)
    } yield result

  end copyCleanViaGit

  @scala.annotation.tailrec
  private def runCommands(commands: Seq[ProcessCmd]): Either[String, Unit] =
    import ProcessDSL.*

    commands match
      case (command @ ProcessCmd(cmds, wd)) +: remainingCommands =>
        val commandAsString = cmds.mkString(" ")
        command.runWithStatus(commandAsString) match
          case r @ Right(_)  => runCommands(remainingCommands)
          case l @ Left(msg) => Left(msg)
      case Nil => Right(())

end Helpers

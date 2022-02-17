package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

final case class StudentifiedSkelFolders(solutionsFolder: File)
object Helpers:

  def fileList(base: File): Vector[File] =
    @scala.annotation.tailrec
    def fileList(filesSoFar: Vector[File], folders: Vector[File]): Vector[File] =
      val subs = (folders foldLeft Vector.empty[File]) {
        case (tally, folder) =>
          tally ++ sbtio.listFiles(folder)
      }
      subs.partition(_.isDirectory) match
        case (rem, result) if rem.isEmpty => filesSoFar ++ result
        case (rem, tally) => fileList(filesSoFar ++ tally, rem)

    val (seedFolders, seedFiles) = sbtio.listFiles(base).partition(_.isDirectory)
    fileList(seedFiles.toVector, seedFolders.toVector)
  end fileList

  def printErrorAndExit(studentifiedRepo: File, message: String): Unit =
    System.err.println(s"${toConsoleRed(message)}")
    System.exit(1)

  def resolveMainRepoPath(mainRepo: File): Either[String, File] = {
    for {
      rp <- getRepoPathFromGit(mainRepo)
    } yield new File(rp)
  }

  private def getRepoPathFromGit(repo: File): Either[String, String] = {
    import ProcessDSL.*
    "git rev-parse --show-toplevel"
      .toProcessCmd(workingDir = repo).runAndReadOutput()
  }

  def exitIfGitIndexOrWorkspaceIsntClean(mainRepo: File): Unit =
    import ProcessDSL.*
    val workspaceIsUnclean = "git status --porcelain"
      .toProcessCmd(workingDir = mainRepo).runAndReadOutput()
      .map(str => str.split("\n").toSeq.map(_.trim).filter(_ != ""))
      .map(_.length)

    workspaceIsUnclean match {
      case Right(cnt) if cnt > 0 =>
        printError(s"main repository isn't clean. Commit changes and try again")(ExitOnFirstError(true))
      case Right(_) => ()
      case Left(_) => 
    }

  def isExerciseFolder(folder: File)(using config: CMTaConfig): Boolean = {

    val ExerciseNameSpec = s""".*[/\\\\]${config.mainRepoExercisePrefix}_\\d{3}_\\w+$$""".r

    ExerciseNameSpec.findFirstIn(folder.getPath).isDefined
  }

  def createStudentifiedFolderSkeleton(stuBase: File, studentifiedRootFolder: File)(using config: CMTaConfig, eofe: ExitOnFirstError) =
    if studentifiedRootFolder.exists then
      System.err.println(printError(s"$studentifiedRootFolder exists already"))
      System.exit(1)
    if !stuBase.canWrite then
      System.err.println(printError(s"$stuBase isn't writeable"))
      System.exit(1)

    val solutionsFolder = studentifiedRootFolder / config.studentifiedRepoSolutionsFolder
  
    sbtio.createDirectories(
      Seq(studentifiedRootFolder, solutionsFolder)
    )
    StudentifiedSkelFolders(solutionsFolder)
  
  def addFirstExercise(cleanedMainRepo: File, firstExercise: String, studentifiedRootFolder: File)
                      (using config: CMTaConfig)=
    sbtio.copyDirectory(cleanedMainRepo / config.mainRepoExerciseFolder / firstExercise,
               studentifiedRootFolder / config.studentifiedRepoActiveExerciseFolder)

  def getExercises(mainRepo: File)(using config: CMTaConfig, eofe: ExitOnFirstError): List[String] =
    sbtio
      .listFiles(isExerciseFolder)(mainRepo /  config.mainRepoExerciseFolder)
      .map(_.getName)
      .to(List)
      .sorted match
        case Nil =>
          System.err.println(printError("No exercises found. Check your configuration")); ???
        case exercises => exercises

  def hideExercises(cleanedMainRepo: File, solutionsFolder: File, exercises: List[String])(using config: CMTaConfig): Unit =
    for (exercise <- exercises)
      val filesToZip = 
        fileList(cleanedMainRepo / config.mainRepoExerciseFolder / exercise)
          .map(f => (f, sbtio.relativize(cleanedMainRepo / config.mainRepoExerciseFolder, f)))
          .collect { case (f, Some(s)) => (f, s)}
      val zipFile = solutionsFolder / s"${exercise}.zip"
      sbtio.zip(filesToZip, zipFile, None)  
  
  def dumpStringToFile(string: String, file: File): Unit =
    import java.nio.charset.StandardCharsets
    import java.nio.file.Files
    Files.write(file.toPath, string.getBytes(StandardCharsets.UTF_8))
  
  def writeStudentifiedCMTConfig(configFile: File, exercises: Seq[String])(using config: CMTaConfig): Unit =
    val cmtConfig =
      s"""studentified-repo-solutions-folder=${config.studentifiedRepoSolutionsFolder}
         |exercise-prefix=${config.mainRepoExercisePrefix}
         |active-exercise-folder=${config.studentifiedRepoActiveExerciseFolder}
         |test-code-folders=${config.testCodeFolders.mkString("[\n   ", ",\n   ", "\n]")}
         |read-me-files=${config.readMeFiles.mkString("[\n   ", ",\n   ", "\n]")}
         |exercises=${exercises.mkString("[\n   ", ",\n   ", "\n]")}
         |cmt-studentified-dont-touch=${config.cmtStudentifiedDontTouch.mkString("[\n   ", ",\n   ", "\n]")}
       """.stripMargin
    dumpStringToFile(cmtConfig, configFile)

  def writeStudentifiedCMTBookmark(bookmarkFile: File, firstExercise: String): Unit =
    dumpStringToFile(firstExercise, bookmarkFile)
  
  def withZipFile(solutionsFolder: File, exerciseID: String)(code: File => Any): Unit =
    val archive = solutionsFolder / s"$exerciseID.zip"
    sbtio.unzip(archive, solutionsFolder)
    code(solutionsFolder / exerciseID)
    sbtio.delete(solutionsFolder / exerciseID)
  end withZipFile
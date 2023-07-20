package com.lunatech.cmt

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

import com.lunatech.cmt.ProcessDSL.ProcessCmd
import com.lunatech.cmt.core.GeneratorInfo
import com.lunatech.cmt.core.command.Package.*
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

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

    if base.isFile then Vector(base)
    else
      val (seedFolders, seedFiles) =
        sbtio.listFiles(base).partition(_.isDirectory)
      fileList(seedFiles.toVector, seedFolders.toVector)
  end fileList

  def resolveMainRepoPath(mainRepo: File): Either[CmtError, File] = {
    for {
      rp <- getRepoPathFromGit(mainRepo)
    } yield new File(rp)
  }

  private def getRepoPathFromGit(repo: File): Either[CmtError, String] = {
    import ProcessDSL.*
    "git rev-parse --show-toplevel".toProcessCmd(workingDir = repo).runAndReadOutput()
  }

  def exitIfGitIndexOrWorkspaceIsntClean(mainRepo: File): Either[CmtError, Unit] =
    import ProcessDSL.toProcessCmd
    val workspaceIsUnclean = "git status --porcelain"
      .toProcessCmd(workingDir = mainRepo)
      .runAndReadOutput()
      .map(str => str.split("\n").toSeq.map(_.trim).filter(_ != ""))
      .map(_.length)

    workspaceIsUnclean match {
      case Right(cnt) if cnt > 0 =>
        Left("Main repository isn't clean. Commit changes and try again".toExecuteCommandErrorMessage)
      case Right(_)  => Right(())
      case Left(msg) => Left(msg)
    }

  def checkpreExistingAndCreateArtifactRepo(
      artifactBaseDirectory: File,
      artifactRootFolder: File,
      forceDeleteDestinationDirectory: Boolean): Either[CmtError, String] =
    (artifactRootFolder.exists, forceDeleteDestinationDirectory) match
      case (true, true) =>
        if artifactBaseDirectory.canWrite then
          sbtio.delete(artifactRootFolder)
          sbtio.createDirectory(artifactRootFolder)
          Right("Created artifact folder")
        else Left(FailedToExecuteCommand(ErrorMessage(s"${artifactBaseDirectory.getPath} isn't writeable")))

      case (true, false) =>
        Left(FailedToExecuteCommand(ErrorMessage(s"$artifactRootFolder exists already")))

      case (false, _) =>
        if artifactBaseDirectory.canWrite then
          sbtio.createDirectory(artifactRootFolder)
          Right("Created artifact folder")
        else Left(FailedToExecuteCommand(ErrorMessage(s"${artifactBaseDirectory.getPath} isn't writeable")))
  end checkpreExistingAndCreateArtifactRepo

  def addFirstExercise(cleanedMainRepo: File, firstExercise: String, studentifiedRootFolder: File)(
      config: CMTaConfig): Unit =
    sbtio.copyDirectory(
      cleanedMainRepo / config.mainRepoExerciseFolder / firstExercise,
      studentifiedRootFolder / config.studentifiedRepoActiveExerciseFolder)
  end addFirstExercise

  final case class ExercisesMetadata(exercisePrefix: String, exercises: Vector[String], exerciseNumbers: Vector[Int])

  def getExerciseMetadata(mainRepo: File)(config: CMTaConfig): Either[CmtError, ExercisesMetadata] =
    val PrefixSpec = raw"(.*)_\d{3}_\w+$$".r
    val matchedNames =
      sbtio.listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder).map(_.getName).to(List)
    val prefixes = matchedNames.map { case PrefixSpec(n) => n }.to(Set)
    sbtio.listFiles(isExerciseFolder())(mainRepo / config.mainRepoExerciseFolder).map(_.getName).to(Vector).sorted match
      case Vector() =>
        Left("No exercises found. Check your configuration".toExecuteCommandErrorMessage)
      case exercises =>
        prefixes.size match
          case 0 => Left("No exercises found".toExecuteCommandErrorMessage)
          case 1 =>
            val exerciseNumbers = exercises.map(extractExerciseNr)
            if exerciseNumbers.size == exerciseNumbers.to(Set).size then
              Right(ExercisesMetadata(prefixes.head, exercises, exerciseNumbers))
            else Left("Duplicate exercise numbers found".toExecuteCommandErrorMessage)
          case _ => Left(s"Multiple exercise prefixes (${prefixes.mkString(", ")}) found".toExecuteCommandErrorMessage)
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

  def extractUniquePaths(paths: Seq[String]): (Seq[String], Seq[String]) =

    @scala.annotation.tailrec
    def fmsp(
        paths: Seq[String],
        prefix: String,
        unique: Seq[String],
        redundant: Seq[String]): (Seq[String], Seq[String]) =
      paths match {
        case Nil =>
          (unique, redundant)
        case p +: remainder =>
          if (p.startsWith(prefix))
            fmsp(remainder, prefix, unique, p +: redundant)
          else
            fmsp(remainder, p, p +: unique, redundant)
      }
    end fmsp

    if (paths.isEmpty) (paths, Seq.empty)
    else {
      val pathsSorted = paths.sorted
      fmsp(pathsSorted.tail, pathsSorted.head, List(pathsSorted.head), List.empty)
    }
  end extractUniquePaths

  def hideExercises(cleanedMainRepo: File, solutionsFolder: File, exercises: Vector[String])(config: CMTaConfig): Unit =
    val now: Option[Long] = Some(java.time.Instant.now().toEpochMilli())
    for (exercise <- exercises)
      zipAndDeleteOriginal(cleanedMainRepo / config.mainRepoExerciseFolder, solutionsFolder, exercise, now)
  end hideExercises

  def dumpStringToFile(string: String, file: File): Unit =
    import java.nio.charset.StandardCharsets
    import java.nio.file.Files
    val _ = Files.write(file.toPath, string.getBytes(StandardCharsets.UTF_8))

  private def dontTouchExtraFiles(config: CMTaConfig): List[String] =
    if config.studentifiedRepoActiveExerciseFolder == "." then List(".cmt", ".git", ".gitignore")
    else List.empty[String]

  private def dontTouchFilesAdjust(config: CMTaConfig, path: String): String =
    if config.studentifiedRepoActiveExerciseFolder == "." then path
    else s"${config.studentifiedRepoActiveExerciseFolder}/${path}"

  def writeStudentifiedCMTConfig(configFile: File, exercises: Seq[String])(
      config: CMTaConfig,
      generatorInfo: GeneratorInfo): Unit =
    val configMap = Map(
      "generator-info" -> Map(
        "generator-name" -> generatorInfo.generatorName,
        "generator-version" -> generatorInfo.generatorVersion).asJava,
      "studentified-repo-solutions-folder" -> config.studentifiedRepoSolutionsFolder,
      "studentified-saved-states-folder" -> config.studentifiedSavedStatesFolder,
      "studentified-repo-bookmark-file" -> config.studentifiedRepoBookmarkFile,
      "test-code-size-and-checksums" -> config.testCodeSizeAndChecksums,
      "code-size-and-checksums" -> config.codeSizeAndChecksums,
      "active-exercise-folder" -> config.studentifiedRepoActiveExerciseFolder,
      "test-code-folders" -> config.testCodeFolders.asJava,
      "read-me-files" -> config.readMeFiles.asJava,
      "exercises" -> exercises.asJava,
      "cmt-studentified-dont-touch" -> (config.cmtStudentifiedDontTouch.map(path =>
        dontTouchFilesAdjust(config, path)) ++ dontTouchExtraFiles(config)).asJava)
    val cmtConfig =
      ConfigFactory.parseMap(configMap.asJava).root().render(ConfigRenderOptions.concise().setFormatted(true))
    dumpStringToFile(cmtConfig, configFile)

  def writeStudentifiedCMTBookmark(bookmarkFile: File, firstExercise: String): Unit =
    dumpStringToFile(firstExercise, bookmarkFile)

  def withZipFile(solutionsFolder: File, exerciseID: String)(
      code: File => Either[CmtError, String]): Either[CmtError, String] =
    val archive = solutionsFolder / s"$exerciseID.zip"
    sbtio.unzip(archive, solutionsFolder)
    val retVal = code(solutionsFolder / exerciseID)
    sbtio.delete(solutionsFolder / exerciseID)
    retVal
  end withZipFile

  def deleteFileIfExists(file: File): Unit =
    if (file.exists()) sbtio.delete(file)
  def initializeGitRepo(linearizedProject: File): Either[CmtError, Unit] =
    import ProcessDSL.toProcessCmd
    s"git init"
      .toProcessCmd(workingDir = linearizedProject)
      .runWithStatus(toConsoleRed(s"Failed to initialize linearized git repository in ${linearizedProject.getPath}"))
  end initializeGitRepo

  def setGitConfig(linearizedProject: File): Either[CmtError, Unit] =
    import ProcessDSL.toProcessCmd
    s"git config --local init.defaultBranch main"
      .toProcessCmd(workingDir = linearizedProject)
      .runWithStatus(toConsoleRed(s"Failed to default branch name in ${linearizedProject.getPath}"))
    s"""git config --local user.email "eric.loots@toto.com""""
      .toProcessCmd(workingDir = linearizedProject)
      .runWithStatus(toConsoleRed(s"Failed to set 'user.mail' in git configuration"))
    s"""git config --local user.name "Eric Loots""""
      .toProcessCmd(workingDir = linearizedProject)
      .runWithStatus(toConsoleRed(s"Failed to set 'user.name' in git configuration"))
  end setGitConfig

  def commitToGit(commitMessage: String, projectFolder: File): Either[CmtError, Unit] =
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

  val ignoreProcessStdOutStdErr: sys.process.ProcessLogger =
    sys.process.ProcessLogger(_ => (), _ => ())
  def copyCleanViaGit(mainRepo: File, tmpDir: File, repoName: String): Either[CmtError, Unit] =

    import ProcessDSL.*

    import java.util.UUID
    val initBranch = UUID.randomUUID.toString
    val tmpRemoteBranch = s"CMT-${UUID.randomUUID.toString}"
    val bareRepoFolder = tmpDir / s"${repoName}.git"
    // @formatter:off
    val script = List(
      (s"${tmpDir.getPath}",
        List(s"git init --bare ${repoName}.git")
      ),
      (
        s"${mainRepo.getPath}",
        List(
          s"git remote add ${tmpRemoteBranch} ${tmpDir.getPath}/${repoName}.git",
          s"git push ${tmpRemoteBranch} HEAD:refs/heads/${initBranch}")
      ),
      (s"${tmpDir.getPath}",
        List(s"git clone -b ${initBranch} ${tmpDir.getPath}/${repoName}.git")
      ),
      (s"${mainRepo.getPath}",
        List(s"git remote remove ${tmpRemoteBranch}")
      )
    )
    // @formatter:on
    val commands = for {
      (workingDir, commands) <- script
      command <- commands
    } yield command.toProcessCmd(new File(workingDir))
    sbtio.delete(bareRepoFolder)
    for {
      result <- runCommands(commands)
    } yield result

  end copyCleanViaGit

  @scala.annotation.tailrec
  private def runCommands(commands: Seq[ProcessCmd]): Either[CmtError, Unit] =
    import ProcessDSL.*

    commands match
      case (command @ ProcessCmd(cmds, wd)) +: remainingCommands =>
        val commandAsString = cmds.mkString(" ")
        command.runWithStatus(commandAsString) match
          case r @ Right(_)  => runCommands(remainingCommands)
          case l @ Left(msg) => l
      case Nil => Right(())

  private val separatorChar: Char = java.io.File.separatorChar

  def adaptToNixSeparatorChar(path: String): String =
    separatorChar match
      case '\\' =>
        path.replaceAll("""\\""", "/")
      case '/' =>
        path
      case _ =>
        path.replaceAll(s"/", s"$separatorChar")

  def adaptToOSSeparatorChar(path: String): String =
    separatorChar match
      case '\\' =>
        path.replaceAll("/", """\\""")
      case '/' =>
        path
      case _ =>
        path.replaceAll(s"/", s"$separatorChar")

  import org.apache.commons.codec.binary.Hex

  import java.nio.file.Files
  import java.security.MessageDigest
  def fileSize(f: File): Long =
    Files.size(f.toPath)

  def fileSha256Hex(f: File): String =
    val fileContents = Files.readAllBytes(f.toPath)
    val digest = MessageDigest.getInstance("SHA-256").digest(fileContents)
    Hex.encodeHexString(digest)

  def exerciseFileHasBeenModified(
      activeExerciseFolder: File,
      file: String,
      fileMetadata: Map[String, FileMetadata]): Boolean =
    fileSize(activeExerciseFolder / file) != fileMetadata(file).size || fileSha256Hex(
      activeExerciseFolder / file) != fileMetadata(file).sha256

  def getFilesToCopyAndDelete(
      currentExerciseId: String,
      toExerciseId: String,
      config: CMTcConfig): (Set[String], Set[String], Set[String]) =
    val currentReadmeFiles = config.readmeFilesMetaData(currentExerciseId).keys.to(Set)
    val nextReadmeFiles = config.readmeFilesMetaData(toExerciseId).keys.to(Set)
    val nextTestCodeFiles = config.testCodeMetaData(toExerciseId).keys.to(Set)

    val currentTestCodeFiles = config.testCodeMetaData(currentExerciseId).keys.to(Set)
    val readmefilesToBeDeleted = currentReadmeFiles &~ nextReadmeFiles
    val readmeFilesToBeCopied = nextReadmeFiles &~ readmefilesToBeDeleted
    val testCodeFilesToBeDeleted = currentTestCodeFiles &~ nextTestCodeFiles
    val testCodeFilesToBeCopied = nextTestCodeFiles &~ testCodeFilesToBeDeleted

    (
      currentTestCodeFiles,
      readmefilesToBeDeleted ++ testCodeFilesToBeDeleted,
      readmeFilesToBeCopied ++ testCodeFilesToBeCopied)

  def pullTestCode(
      toExerciseId: String,
      activeExerciseFolder: File,
      filesToBeDeleted: Set[String],
      filesToBeCopied: Set[String],
      config: CMTcConfig): Either[CmtError, String] =
    withZipFile(config.solutionsFolder, toExerciseId) { solution =>
      for {
        file <- filesToBeDeleted
      } deleteFileIfExists(activeExerciseFolder / file)
      for {
        file <- filesToBeCopied
      } sbtio.copyFile(solution / file, activeExerciseFolder / file)

      writeStudentifiedCMTBookmark(config.bookmarkFile, toExerciseId)

      Right(s"${toConsoleGreen("Moved to ")} " + "" + s"${toConsoleYellow(s"$toExerciseId")}")
    }
  def writeTestReadmeCodeMetadata(
      cleanedMainRepo: File,
      exercises: Vector[String],
      studentifiedRootFolder: File,
      cmtaConfig: CMTaConfig): Unit =

    val testCodeFolders = cmtaConfig.testCodeFolders.to(List)

    import scala.jdk.CollectionConverters.*

    sbtio.createDirectory(studentifiedRootFolder / cmtaConfig.cmtMetadataRootFolder)

    val testCodeFilesInExercises = (for {
      exercise <- exercises
      (srcTestCodeFiles, srcTestCodeFolders) =
        testCodeFolders
          .map(f => cleanedMainRepo / cmtaConfig.mainRepoExerciseFolder / exercise / f)
          .partition(f => f.isFile)
      allFiles =
        (srcTestCodeFiles ++ srcTestCodeFolders.flatMap(fileList))
          .map(f => (sbtio.relativizeFile(cleanedMainRepo / cmtaConfig.mainRepoExerciseFolder / exercise, f), f))
          .collect { case (Some(s), f) =>
            Map(
              s""""${adaptToNixSeparatorChar(s.getPath)}"""" -> Map(
                "size" -> fileSize(f),
                "sha256" -> fileSha256Hex(f)).asJava).asJava
          }

    } yield exercise -> allFiles.asJava).to(Map)

    val readmeFilesInExercises = (for {
      exercise <- exercises
      (srcReadmeFiles, srcReadmeFolders) =
        cmtaConfig.readMeFiles
          .map(f => cleanedMainRepo / cmtaConfig.mainRepoExerciseFolder / exercise / f)
          .partition(f => f.isFile)
      allFiles =
        (srcReadmeFiles ++ srcReadmeFolders.flatMap(fileList))
          .map(f => (sbtio.relativizeFile(cleanedMainRepo / cmtaConfig.mainRepoExerciseFolder / exercise, f), f))
          .collect { case (Some(s), f) =>
            Map(
              s""""${adaptToNixSeparatorChar(s.getPath)}"""" -> Map(
                "size" -> fileSize(f),
                "sha256" -> fileSha256Hex(f)).asJava).asJava
          }

    } yield exercise -> allFiles.asJava).to(Map)

    val cfgTestCode = ConfigFactory
      .parseMap(Map("testcode-metadata" -> testCodeFilesInExercises.asJava).asJava)
      .root()
      .render(ConfigRenderOptions.concise().setJson(false).setFormatted(true))
    val cfgReadmeFiles = ConfigFactory
      .parseMap(Map("readmefiles-metadata" -> readmeFilesInExercises.asJava).asJava)
      .root()
      .render(ConfigRenderOptions.concise().setJson(false).setFormatted(true))
    val metadataConfig = s"$cfgTestCode\n\n$cfgReadmeFiles"
    dumpStringToFile(metadataConfig, studentifiedRootFolder / cmtaConfig.testCodeSizeAndChecksums)
  end writeTestReadmeCodeMetadata

  def writeCodeMetadata(
      cleanedMainRepo: File,
      exercises: Vector[String],
      studentifiedRootFolder: File,
      cmtaConfig: CMTaConfig): Unit =

    import scala.jdk.CollectionConverters.*

    val codeFilesInExercises = (for {
      exercise <- exercises
      allFiles =
        fileList(cleanedMainRepo / cmtaConfig.mainRepoExerciseFolder / exercise)
          .map(f => (sbtio.relativizeFile(cleanedMainRepo / cmtaConfig.mainRepoExerciseFolder / exercise, f), f))
          .collect { case (Some(s), f) =>
            Map(
              s""""${adaptToNixSeparatorChar(s.getPath)}"""" -> Map(
                "size" -> fileSize(f),
                "sha256" -> fileSha256Hex(f)).asJava).asJava
          }

    } yield exercise -> allFiles.asJava).to(Map)

    val cfgCode = ConfigFactory
      .parseMap(Map("code-metadata" -> codeFilesInExercises.asJava).asJava)
      .root()
      .render(ConfigRenderOptions.concise().setJson(false).setFormatted(true))
    val metadataConfig = s"$cfgCode\n"
    dumpStringToFile(metadataConfig, studentifiedRootFolder / cmtaConfig.codeSizeAndChecksums)
  end writeCodeMetadata

  extension (f: File)
    // Gets the parent folder of this folder but return this
    // folder if it's a root folder
    def getParentOrSelf: File =
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
        val pf = path.getParentOrSelf
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

end Helpers

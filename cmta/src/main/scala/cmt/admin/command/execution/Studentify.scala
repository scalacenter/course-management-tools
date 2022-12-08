package cmt.admin.command.execution

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

import cmt.Helpers.*
import cmt.admin.command.AdminCommand.Studentify
import cmt.core.execution.Executable
import cmt.{Helpers, StudentifiedSkelFolders, toConsoleGreen}
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.CmtError

given Executable[Studentify] with
  extension (cmd: Studentify)
    def execute(): Either[CmtError, String] =
      import StudentifyHelpers.*

      def checkForOverlappingPathsInConfig(): Unit =
        val (_, redundantPaths) =
          Helpers.extractUniquePaths(cmd.config.testCodeFolders.to(List) ++ cmd.config.readMeFiles.to(List))
        if (redundantPaths.nonEmpty)
          for (redundantPath <- redundantPaths)
            println(cmt.toConsoleYellow(s"WARNING: Redundant path detected in CMT configuration: $redundantPath"))

      checkForOverlappingPathsInConfig()

      for {
        _ <- exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)

        _ = println(s"Studentifying ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
            cmd.studentifyBaseDirectory.value.getPath)}")

        mainRepoName = cmd.mainRepository.value.getName
        tmpFolder = sbtio.createTemporaryDirectory
        cleanedMainRepo = tmpFolder / mainRepoName
        studentifiedRootFolder = cmd.studentifyBaseDirectory.value / mainRepoName
        solutionsFolder = studentifiedRootFolder / cmd.config.studentifiedRepoSolutionsFolder

        _ = checkpreExistingAndCreateArtifactRepo(
          cmd.studentifyBaseDirectory.value,
          studentifiedRootFolder,
          cmd.forceDeleteDestinationDirectory.value)

        _ = sbtio.createDirectory(cmd.studentifyBaseDirectory.value / cmd.config.studentifiedRepoSolutionsFolder)

        _ <- copyCleanViaGit(cmd.mainRepository.value, tmpFolder, mainRepoName)

        ExercisesMetadata(prefix, exercises, exerciseNumbers) <- getExerciseMetadata(cmd.mainRepository.value)(
          cmd.config)

        _ = buildStudentifiedRepository(
          cleanedMainRepo,
          exercises,
          studentifiedRootFolder,
          solutionsFolder,
          cmd,
          tmpFolder)

        successMessage <- Right(exercises.mkString("Processed exercises:\n  ", "\n  ", "\n"))

      } yield successMessage
end given

private object StudentifyHelpers:
  def buildStudentifiedRepository(
      cleanedMainRepo: File,
      exercises: Vector[String],
      studentifiedRootFolder: File,
      solutionsFolder: File,
      cmd: Studentify,
      tmpFolder: File): Either[CmtError, String] =

    addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)(cmd.config)

    writeTestReadmeCodeMetadata(cleanedMainRepo, exercises, studentifiedRootFolder, cmd.config)

    hideExercises(cleanedMainRepo, solutionsFolder, exercises)(cmd.config)

    writeStudentifiedCMTConfig(studentifiedRootFolder / cmd.config.cmtStudentifiedConfigFile, exercises)(cmd.config)
    writeStudentifiedCMTBookmark(studentifiedRootFolder / cmd.config.studentifiedRepoBookmarkFile, exercises.head)

    val successMessage = exercises.mkString("Processed exercises:\n  ", "\n  ", "\n")
    if cmd.initializeAsGitRepo.value then
      val dotIgnoreFile = cleanedMainRepo / ".gitignore"
      if dotIgnoreFile.exists then sbtio.copyFile(dotIgnoreFile, studentifiedRootFolder / ".gitignore")
      for {
        _ <- initializeGitRepo(studentifiedRootFolder)
        _ <- commitToGit("Initial commit", studentifiedRootFolder)
        _ = sbtio.delete(tmpFolder)
        result <- Right(successMessage)
      } yield result
    else
      sbtio.delete(tmpFolder)
      Right(successMessage)
  end buildStudentifiedRepository
end StudentifyHelpers

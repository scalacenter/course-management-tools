package cmt.admin.command.execution

import cmt.Helpers.*
import cmt.admin.command.AdminCommand.Studentify
import cmt.core.execution.Executable
import cmt.{StudentifiedSkelFolders, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*

given Executable[Studentify] with
  extension (cmd: Studentify)
    def execute(): Either[String, String] =
      import StudentifyHelpers.*

      for {
        _ <- exitIfGitIndexOrWorkspaceIsntClean(cmd.mainRepository.value)
        _ = println(s"Studentifying ${toConsoleGreen(cmd.mainRepository.value.getPath)} to ${toConsoleGreen(
            cmd.studentifyBaseDirectory.value.getPath)}")

        mainRepoName = cmd.mainRepository.value.getName
        tmpFolder = sbtio.createTemporaryDirectory
        cleanedMainRepo = tmpFolder / cmd.mainRepository.value.getName

        _ <- copyCleanViaGit(cmd.mainRepository.value, tmpFolder, mainRepoName)

        ExercisesMetadata(prefix, exercises, exerciseNumbers) <- getExerciseMetadata(cmd.mainRepository.value)(
          cmd.config)
        studentifiedRootFolder = cmd.studentifyBaseDirectory.value / mainRepoName

        _ = if studentifiedRootFolder.exists && cmd.forceDeleteDestinationDirectory.value then
          sbtio.delete(studentifiedRootFolder)

        StudentifiedSkelFolders(solutionsFolder) =
          createStudentifiedFolderSkeleton(cmd.studentifyBaseDirectory.value, studentifiedRootFolder)(cmd.config)

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
      tmpFolder: File): Either[String, String] =
    addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)(cmd.config)

    hideExercises(cleanedMainRepo, solutionsFolder, exercises)(cmd.config)

    writeStudentifiedCMTConfig(studentifiedRootFolder / cmd.config.cmtStudentifiedConfigFile, exercises)(cmd.config)
    writeStudentifiedCMTBookmark(studentifiedRootFolder / ".bookmark", exercises.head)

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

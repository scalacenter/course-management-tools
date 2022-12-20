package cmt.admin.command

import caseapp.core.Error
import caseapp.{AppName, Command, CommandName, ExtraName, HelpMessage, Name, Recurse, RemainingArgs}
import cmt.Helpers.*
import cmt.admin.Domain.{ForceDeleteDestinationDirectory, InitializeGitRepo, StudentifyBaseDirectory}
import cmt.core.execution.Executable
import cmt.{CMTaConfig, CmtError, StudentifiedSkelFolders, printResult, toCmtError, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import cmt.admin.*
import cmt.admin.cli.SharedOptions
import cmt.core.validation.Validatable
import cmt.admin.cli.ArgParsers.{
  forceDeleteDestinationDirectoryArgParser,
  initializeGitRepoArgParser,
  studentifyBaseDirectoryArgParser
}
import cmt.core.CmtCommand
import cmt.toCmtError
import cmt.toExecuteCommandErrorMessage

object Studentify:

  @AppName("studentify")
  @CommandName("studentify")
  @HelpMessage(
    "'studentifies' an existing repository - taking the 'master' repository and creating a CMT project in the target directory")
  case class Options(
      @ExtraName("s")
      studentifyBaseDirectory: StudentifyBaseDirectory,
      @HelpMessage(
        "if set to 'true' the destination directory in which the studentified project is created will be wiped before the new studentified project is created")
      @ExtraName("f")
      forceDelete: ForceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(false),
      @HelpMessage("if set to 'true' the destination directory will be created as a git repository")
      @ExtraName("g")
      initGit: InitializeGitRepo = InitializeGitRepo(false),
      @Recurse shared: SharedOptions)

  given Validatable[Studentify.Options] with
    extension (options: Studentify.Options)
      def validated(): Either[CmtError, Studentify.Options] =
        if (options.studentifyBaseDirectory.value.equals(options.shared.mainRepository.value)) {
          Left("main repository cannot be the same as the destination directory".toExecuteCommandErrorMessage)
        } else {
          Right(options)
        }
      end validated
  end given

  given Executable[Studentify.Options] with
    extension (options: Studentify.Options)
      def execute(): Either[CmtError, String] =
        import StudentifyHelpers.*

        val mainRepository = options.shared.mainRepository
        val config = new CMTaConfig(mainRepository.value, options.shared.maybeConfigFile.map(_.value))

        def checkForOverlappingPathsInConfig(): Unit =
          val (_, redundantPaths) =
            extractUniquePaths(config.testCodeFolders.to(List) ++ config.readMeFiles.to(List))
          if (redundantPaths.nonEmpty)
            for (redundantPath <- redundantPaths)
              println(cmt.toConsoleYellow(s"WARNING: Redundant path detected in CMT configuration: $redundantPath"))

        checkForOverlappingPathsInConfig()

        for {
          _ <- exitIfGitIndexOrWorkspaceIsntClean(mainRepository.value)

          _ = println(s"Studentifying ${toConsoleGreen(mainRepository.value.getPath)} to ${toConsoleGreen(
              options.studentifyBaseDirectory.value.getPath)}")

          mainRepoName = mainRepository.value.getName
          tmpFolder = sbtio.createTemporaryDirectory
          cleanedMainRepo = tmpFolder / mainRepoName
          studentifiedRootFolder = options.studentifyBaseDirectory.value / mainRepoName
          solutionsFolder = studentifiedRootFolder / config.studentifiedRepoSolutionsFolder

          _ = checkpreExistingAndCreateArtifactRepo(
            options.studentifyBaseDirectory.value,
            studentifiedRootFolder,
            options.forceDelete.value)

          _ = sbtio.createDirectory(options.studentifyBaseDirectory.value / config.studentifiedRepoSolutionsFolder)

          _ <- copyCleanViaGit(mainRepository.value, tmpFolder, mainRepoName)

          ExercisesMetadata(prefix, exercises, exerciseNumbers) <- getExerciseMetadata(mainRepository.value)(config)

          _ = buildStudentifiedRepository(
            cleanedMainRepo,
            exercises,
            studentifiedRootFolder,
            solutionsFolder,
            config,
            options.initGit,
            tmpFolder)

          successMessage <- Right(exercises.mkString("Processed exercises:\n  ", "\n  ", "\n"))

        } yield successMessage
      end execute
  end given

  private object StudentifyHelpers:
    def buildStudentifiedRepository(
        cleanedMainRepo: File,
        exercises: Vector[String],
        studentifiedRootFolder: File,
        solutionsFolder: File,
        config: CMTaConfig,
        initializeAsGitRepo: InitializeGitRepo,
        tmpFolder: File): Either[CmtError, String] =

      addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)(config)

      writeTestReadmeCodeMetadata(cleanedMainRepo, exercises, studentifiedRootFolder, config)

      hideExercises(cleanedMainRepo, solutionsFolder, exercises)(config)

      writeStudentifiedCMTConfig(studentifiedRootFolder / config.cmtStudentifiedConfigFile, exercises)(config)
      writeStudentifiedCMTBookmark(studentifiedRootFolder / config.studentifiedRepoBookmarkFile, exercises.head)

      val successMessage = exercises.mkString("Processed exercises:\n  ", "\n  ", "\n")
      if initializeAsGitRepo.value then
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

  val command = new CmtCommand[Studentify.Options] {

    def run(options: Studentify.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end Studentify

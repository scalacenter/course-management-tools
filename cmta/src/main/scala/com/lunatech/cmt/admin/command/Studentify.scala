package com.lunatech.cmt.admin.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, Name, Recurse, RemainingArgs, ValueDescription}
import com.lunatech.cmt.Helpers.*
import com.lunatech.cmt.admin.Domain.{
  ForceDeleteDestinationDirectory,
  InitializeGitRepo,
  MainRepository,
  StudentifyBaseDirectory
}
import com.lunatech.cmt.core.execution.Executable
import com.lunatech.cmt.{CMTaConfig, CmtError, printResult, toConsoleGreen}
import sbt.io.IO as sbtio
import sbt.io.syntax.*
import com.lunatech.cmt.admin.validateDestinationFolder
import com.lunatech.cmt.admin.cli.SharedOptions
import com.lunatech.cmt.core.validation.Validatable
import com.lunatech.cmt.admin.cli.ArgParsers.{
  forceDeleteDestinationDirectoryArgParser,
  initializeGitRepoArgParser,
  studentifyBaseDirectoryArgParser
}
import com.lunatech.cmt.core.GeneratorInfo
import com.lunatech.cmt.core.cli.CmtCommand

object Studentify:

  @AppName("studentify")
  @CommandName("studentify")
  @HelpMessage(
    "'Studentifies' an existing repository - taking the 'main' repository and creating a CMT project in the target directory")
  final case class Options(
      @ExtraName("d")
      @ValueDescription("Folder in which the 'studentified' artifact will be created")
      studentifyBaseDirectory: StudentifyBaseDirectory,
      @ExtraName("f")
      @ValueDescription(
        "if set to 'true' the destination directory in which the studentified project is created will be wiped before the new studentified project is created")
      forceDelete: ForceDeleteDestinationDirectory = ForceDeleteDestinationDirectory(false),
      @ExtraName("g")
      @ValueDescription("if set to 'true' the destination directory will be created as a git repository")
      initGit: InitializeGitRepo = InitializeGitRepo(false),
      @Recurse shared: SharedOptions)

  given Validatable[Studentify.Options] with
    extension (options: Studentify.Options)
      def validated(): Either[CmtError, Studentify.Options] =
        for {
          mainRepository <- resolveMainRepoPath(options.shared.mainRepository.value)
          _ <- validateDestinationFolder(
            mainRepository = mainRepository,
            destination = options.studentifyBaseDirectory.value)
        } yield options
      end validated
  end given

  given Executable[Studentify.Options] with
    extension (options: Studentify.Options)
      def execute(): Either[CmtError, String] =
        import StudentifyHelpers.*

        resolveMainRepoPath(options.shared.mainRepository.value).flatMap { repository =>
          val mainRepository = MainRepository(repository)
          val config = new CMTaConfig(mainRepository.value, options.shared.maybeConfigFile.map(_.value))

          def checkForOverlappingPathsInConfig(): Unit =
            val (_, redundantPaths) =
              extractUniquePaths(config.testCodeFolders.to(List) ++ config.readMeFiles.to(List))
            if (redundantPaths.nonEmpty)
              for (redundantPath <- redundantPaths)
                println(
                  com.lunatech.cmt
                    .toConsoleYellow(s"WARNING: Redundant path detected in CMT configuration: $redundantPath"))

          checkForOverlappingPathsInConfig()

          for {
            _ <- exitIfGitIndexOrWorkspaceIsntClean(mainRepository.value)

            mainRepoName = mainRepository.value.getName
            tmpFolder = sbtio.createTemporaryDirectory
            cleanedMainRepo = tmpFolder / mainRepoName
            studentifiedRootFolder = options.studentifyBaseDirectory.value / mainRepoName
            solutionsFolder = studentifiedRootFolder / config.studentifiedRepoSolutionsFolder

            _ = println(
              s"Studentifying ${toConsoleGreen(mainRepoName)} to ${toConsoleGreen(options.studentifyBaseDirectory.value.getPath)}")

            _ <- checkpreExistingAndCreateArtifactRepo(
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
        }
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
      writeCodeMetadata(cleanedMainRepo, exercises, studentifiedRootFolder, config)

      hideExercises(cleanedMainRepo, solutionsFolder, exercises)(config)

      import com.lunatech.cmt.version.BuildInfo
      val generatorInfo = GeneratorInfo(BuildInfo.name, BuildInfo.version)
      writeStudentifiedCMTConfig(studentifiedRootFolder / config.cmtStudentifiedConfigFile, exercises)(
        config,
        generatorInfo)
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

  val command: CmtCommand[Studentify.Options] = new CmtCommand[Studentify.Options] {

    def run(options: Studentify.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end Studentify

package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

import Helpers.{getExercises,
                exitIfGitIndexOrWorkspaceIsntClean,
                createStudentifiedFolderSkeleton,
                addFirstExercise,
                hideExercises,
                writeStudentifiedCMTBookmark,
                writeStudentifiedCMTConfig}

object CMTStudentify:
  def studentify(mainRepo: File, stuBase: File)
                (using config: CMTaConfig, eofe: ExitOnFirstError): Unit =

    exitIfGitIndexOrWorkspaceIsntClean(mainRepo)    

    println(s"Studentifying ${toConsoleGreen(mainRepo.getPath)} to ${toConsoleGreen(stuBase.getPath)}")

    val mainRepoName = mainRepo.getName
    
    val tmpFolder = sbtio.createTemporaryDirectory
    val cleanedMainRepo = ProcessDSL.copyCleanViaGit(mainRepo, tmpFolder, mainRepoName)
    
    val exercises = getExercises(mainRepo)
    val studentifiedRootFolder = stuBase / mainRepoName

    val StudentifiedSkelFolders(solutionsFolder) = 
      createStudentifiedFolderSkeleton(stuBase, studentifiedRootFolder)

    addFirstExercise(cleanedMainRepo, exercises.head, studentifiedRootFolder)

    hideExercises(cleanedMainRepo, solutionsFolder, exercises)

    sbtio.delete(tmpFolder)

    writeStudentifiedCMTConfig(studentifiedRootFolder / config.cmtStudentifiedConfigFile, exercises)
    writeStudentifiedCMTBookmark(studentifiedRootFolder / ".bookmark", exercises.head)

    println(toConsoleGreen(exercises.mkString("Processed exercises:\n  ", "\n  ", "\n")))

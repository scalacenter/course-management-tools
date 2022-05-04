package cmt

import sbt.io.{IO as sbtio}
import sbt.io.syntax.*

import Helpers.*

object CMTStudentify:
  def studentify(mainRepo: File, stuBase: File)
                (using config: CMTConfig, eofe: ExitOnFirstError): Unit =
    
    println(s"Studentifying ${toConsoleGreen(mainRepo.getPath)} to ${toConsoleGreen(stuBase.getPath)}")
    
    exitIfGitIndexOrWorkspaceIsntClean(mainRepo)
    
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

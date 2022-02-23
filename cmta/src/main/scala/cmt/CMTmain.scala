package cmt

object Main:
  def main(args: Array[String]): Unit =
    val cmdLineArgs: CmtaOptions =
      CmdLineParse.cmtaParse(args).getOrElse {
        System.exit(1)
        ???
      }

    given CMTaConfig = CMTaConfig(cmdLineArgs.mainRepo, cmdLineArgs.configFile)

    cmdLineArgs match {
      case CmtaOptions(
            mainRepo,
            Studentify(Some(stuBase), forceDeleteExistingDestinationFolder: Boolean, initializeAsGitRepo: Boolean),
            _) =>
        CMTStudentify.studentify(mainRepo, stuBase, forceDeleteExistingDestinationFolder, initializeAsGitRepo)

      case CmtaOptions(mainRepo, RenumberExercises(renumStartAtOpt, renumOffset, renumStep), configFile) =>
        CMTAdmin.renumberExercises(mainRepo, renumStartAtOpt, renumOffset, renumStep)

      case CmtaOptions(mainRepo, DuplicateInsertBefore(exerciseNumber), configFile) =>
        CMTAdmin.duplicateInsertBefore(mainRepo, exerciseNumber)

      case CmtaOptions(mainRepo, Linearize(Some(linBase), forceDeleteExistingDestinationFolder: Boolean), _) =>
        CMTLinearize.linearize(mainRepo, linBase, forceDeleteExistingDestinationFolder)

      case CmtaOptions(mainRepo, DeLinearize(Some(linBase)), _) =>
        CMTDeLinearize.delinearize(mainRepo, linBase)

      case _ =>
    }

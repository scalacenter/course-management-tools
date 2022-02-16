package cmt

object Main:
  def main(args: Array[String]): Unit =
    val cmdLineArgs: CmtaOptions = 
      CmdLineParse
        .cmtaParse(args)
        .getOrElse {
          System.exit(1)
          ???
        }

    given ExitOnFirstError = ExitOnFirstError(true)
    given CMTaConfig = CMTaConfig(cmdLineArgs.mainRepo, cmdLineArgs.configFile)

    cmdLineArgs match {
      case CmtaOptions(mainRepo, Studentify, _, CmtaStudentifyOptions(Some(stuBase)), _) => 
        CMTStudentify.studentify(mainRepo, stuBase)

      case CmtaOptions(mainRepo, RenumberExercises, CmtaRenumOptions(renumOffset, renumStep), _, configFile) =>
        CMTAdmin.renumberExercises(mainRepo, renumOffset, renumStep)

      case _ =>
    }

    
 

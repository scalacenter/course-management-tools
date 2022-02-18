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
      case CmtaOptions(mainRepo, Studentify(Some(stuBase)), _) =>
        CMTStudentify.studentify(mainRepo, stuBase)

      case CmtaOptions(mainRepo, RenumberExercises(renumOffset, renumStep), configFile) =>
        CMTAdmin.renumberExercises(mainRepo, renumOffset, renumStep)

      case _ =>
    }

    
 

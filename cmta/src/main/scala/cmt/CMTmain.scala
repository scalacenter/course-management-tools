package cmt

object Main:
  def main(args: Array[String]): Unit =
    val cmdLineArgs: CmdOptions = 
      CmdLineParse
        .parse(args)
        .getOrElse {
          System.exit(1)
          ???
        }

    given ExitOnFirstError = ExitOnFirstError(true)
    given CMTConfig = CMTConfig(cmdLineArgs.mainRepo, cmdLineArgs.configFile)

    cmdLineArgs match {
      case CmdOptions(mainRepo, Studentify, _, CmdStudentifyOptions(Some(stuBase)), _, _) => 
        CMTStudentify.studentify(mainRepo, stuBase)

      // case CmdOptions(mainRepo, RenumberExercises, CmdRenumOptions(renumOffset, renumStep), _, configFile) =>
      //   CMTAdmin.renumberExercises(mainRepo, renumOffset, renumStep)
      
      case CmdOptions(_, PullSolution, _, _, CmdPullSolutionOptions(Some(exerciseID), Some(studentifiedRepo)), _) =>
        CMTStudent.pullSolution(studentifiedRepo, exerciseID)

      case _ =>
    }

    
 

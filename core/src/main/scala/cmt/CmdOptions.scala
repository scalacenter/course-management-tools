package cmt

import scopt.OParser
import scopt.OParserBuilder

import sbt.io.syntax.*

sealed trait CMTCommands
case object Missing extends CMTCommands
case object RenumberExercises extends CMTCommands
case object Studentify extends CMTCommands
case object PullSolution extends cmt.CMTCommands

final case class CmdOptions(
  mainRepo: File = new File("."),
  command: CMTCommands = Missing,
  cmdRenumOptions: CmdRenumOptions = CmdRenumOptions(),
  cmdStudentifyOptions: CmdStudentifyOptions = CmdStudentifyOptions(),
  cmdPullSolutionOptions: CmdPullSolutionOptions = CmdPullSolutionOptions(),
  configFile: Option[File] = None
)

final case class CmdRenumOptions(
  renumOffset: Int = 1,
  renumStep: Int = 1
)

final case class CmdPullSolutionOptions(
  exerciseID: Option[String] = None,
  studentifiedRepo: Option[File] = None
)

final case class CmdStudentifyOptions(
  studentifyBaseFolder: Option[File] = None
)

val parser = {
  given builder: OParserBuilder[CmdOptions] = OParser.builder[CmdOptions]
  import builder.*

  OParser.sequence(
    programName("cmt"),
    renumCmdParser,
    studentifyCmdParser,
    configFileParser,
    pullSolutionParser,
    validateConfig 
  )
}

private def mainRepoArgument(using builder: OParserBuilder[CmdOptions]): OParser[File, CmdOptions] =
  import builder.*
    arg[File]("<Main repo>")
    .validate{ f => 
      if f.isDirectory then
        success
      else failure(s"$f is not a directory")
    }
    .action{ (mainRepo, c) => 
      Helpers.resolveMainRepoPath(mainRepo) match {
        case Right(path) =>
          c.copy(mainRepo = path)
        case Left(msg) =>
          printError(s"$mainRepo is not a git repository")(ExitOnFirstError(true));???
      }
    }

private def configFileParser(using builder: OParserBuilder[CmdOptions]): OParser[File, CmdOptions] =
  import builder.*
  opt[File]("configuration")
    .abbr("cfg")
    .text("CMT configuration file")
    .action{(configFile, c) =>
      c.copy(configFile = Some(configFile))
    }


private def studentifyCmdParser(using builder: OParserBuilder[CmdOptions]): OParser[Unit, CmdOptions] =
  import builder.*
  cmd("studentify")
    .text("Generate a studentified repository from a given main repository")
    .action{(_, c) =>
      c.copy(command = Studentify)
    }
    .children(
      mainRepoArgument,
      arg[File]("<studentified repo parent folder>")
        .validate{ baseFolder =>
          (baseFolder.exists, baseFolder.isDirectory) match
            case (true, true) => success
            case (false, _) => failure(s"${baseFolder.getPath} doesn't exist")
            case (_, false) => failure(s"${baseFolder.getPath} is not a directory")
        }
        .action((studRepo, c) =>
          c.copy(cmdStudentifyOptions = c.cmdStudentifyOptions.copy(studentifyBaseFolder = Some(studRepo)))
        )
    )

private def pullSolutionParser(using builder: OParserBuilder[CmdOptions]): OParser[Unit, CmdOptions] =
  import builder.*
  cmd("pull-solution")
    .text("Pull solution for a given exercise")
    .action{(_, c) =>
      c.copy(command = PullSolution)
    }
    .children(
      arg[String]("<exercise ID>")
        .action((exercise, c) =>
          c.copy(cmdPullSolutionOptions = c.cmdPullSolutionOptions.copy(exerciseID = Some(exercise)))
      ),
      arg[File]("<studentified repo  folder>")
        .validate{ studentifiedFolder =>
          if studentifiedFolder.exists
          then success
          else failure(s"$studentifiedFolder: doesn't exist")
        }
        .action{(repo, c) =>
          c.copy(cmdPullSolutionOptions = c.cmdPullSolutionOptions.copy(studentifiedRepo = Some(repo)))
        }
      
    )

private def renumCmdParser(using builder: OParserBuilder[CmdOptions]): OParser[Unit, CmdOptions] =
  import builder.*
  cmd("renum")
      .text("Renumber exercises starting at a given offset and increment by a given step size")
      .action{(_, c) =>
        c.copy(command = RenumberExercises)
      }
      .children(
        mainRepoArgument,
        opt[Int]("offset")
          .text("renumber start offset (default=1)")
          .abbr("o")
          .validate(offset => if offset >= 0 then success else failure(s"renumber offset should be >= 0"))
          .action{(offset, c) =>
            c.copy(cmdRenumOptions = c.cmdRenumOptions.copy(renumOffset = offset))
          },
        opt[Int]("step")
          .text("renumber step size (default=1)")
          .abbr("s")
          .validate(step => if step >= 1 then success else failure(s"renumber step size should be >= 1"))
          .action{(step, c) =>
            c.copy(cmdRenumOptions = c.cmdRenumOptions.copy(renumStep = step))
          }
      )

private def validateConfig(using builder: OParserBuilder[CmdOptions]): OParser[Unit, CmdOptions] =
  import builder.*
  checkConfig(config => config.command match
    case Missing => failure("missing command")
    case PullSolution => success
    case _ => success
  )

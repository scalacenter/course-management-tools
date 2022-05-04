package cmt

import scala.sys.process.Process
import scala.util.{Failure, Success, Try}

import sbt.io.syntax.*

object ProcessDSL:
  final case class CmdWithsWorkingDir(cmd: String, workingDir: String)

  final case class ProcessCmd(cmd: Seq[String], workingDir: File)

  extension (cmd: ProcessCmd) 
    def runAndExitIfFailed(errorMsg: String): Unit = 
      val status = Try(Process(cmd.cmd, cmd.workingDir).!)
      if status.getOrElse(-1) != 0 then
        System.err.println(s"""
                   |$errorMsg
                   |  Executed command: ${cmd.cmd.mkString(" ")}
                   |  Working directory: ${cmd.workingDir}
           """.stripMargin)
        System.exit(status.getOrElse(-1))

    def run: Int = {
      val status = Try(Process(cmd.cmd, cmd.workingDir).!)
      status.getOrElse(-1)
    }

    def runAndReadOutput(): Either[String, String] = 
      val consoleRes = Try(Process(cmd.cmd, cmd.workingDir).!!)
      consoleRes match 
        case Success(result) => Right(result.trim)
        case Failure(_) =>
          val msg = s"""
                       |  Executed command: ${cmd.cmd.mkString(" ")}
                       |  Working directory: ${cmd.workingDir}
          """.stripMargin
          Left(msg)
  end extension
          

  extension (command: String)
    def toProcessCmd(workingDir: File): ProcessCmd =
      val SplitRegex = "([^\"]\\S*|\".+?\")\\s*".r
      val cmdArgs =
        SplitRegex
          .findAllMatchIn(command)
          .map(_.toString.replaceAll(" $", "").replaceAll(""""""", ""))
          .toVector
      ProcessCmd(cmdArgs, workingDir)

  def copyCleanViaGit(mainRepo: File, tmpDir: File, repoName: String): File =

    import java.util.UUID
    val initBranch = UUID.randomUUID.toString
    val tmpRemote = s"CMT-${UUID.randomUUID.toString}"
    val script = List(
      (s"${tmpDir.getPath}",
        List(
          s"mkdir ${repoName}.git",
          s"git init --bare ${repoName}.git"
        )
      ),
      (s"${mainRepo.getPath}",
        List(
          s"git remote add ${tmpRemote} ${tmpDir.getPath}/${repoName}.git",
          s"git push ${tmpRemote} HEAD:refs/heads/${initBranch}"
        )
      ),
      (s"${tmpDir.getPath}",
       List(
         s"git clone -b ${initBranch} ${tmpDir.getPath}/${repoName}.git",
         s"rm -rf ${tmpDir.getPath}/${repoName}.git"
       )
      ),
      (s"${mainRepo.getPath}",
        List(
          s"git remote remove ${tmpRemote}"
        )
      )
    )
    val commands = for {
      (workingDir, commands) <- script
      command <- commands
    } yield command.toProcessCmd(new File(workingDir))

    for {
        command @ ProcessCmd(cmds, wd) <- commands
        cmdAsString = cmds.mkString(" ")
      } command.runAndExitIfFailed(cmdAsString)

    tmpDir / mainRepo.getName
  end copyCleanViaGit

end ProcessDSL
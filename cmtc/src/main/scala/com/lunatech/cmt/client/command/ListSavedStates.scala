package com.lunatech.cmt.client.command

import caseapp.{AppName, CommandName, ExtraName, HelpMessage, RemainingArgs}
import com.lunatech.cmt.client.Configuration
import com.lunatech.cmt.Domain.StudentifiedRepo
import com.lunatech.cmt.client.cli.CmtcCommand
import com.lunatech.cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow}
import com.lunatech.cmt.core.validation.Validatable
import sbt.io.IO as sbtio
import com.lunatech.cmt.core.cli.enforceNoTrailingArguments
import com.lunatech.cmt.client.cli.ArgParsers.studentifiedRepoArgParser

object ListSavedStates:

  @AppName("list-saved-states")
  @CommandName("list-saved-states")
  @HelpMessage("List all saved exercise states, if any.")
  final case class Options(
      @ExtraName("s")
      studentifiedRepo: Option[StudentifiedRepo] = None)

  given Validatable[ListSavedStates.Options] with
    extension (options: ListSavedStates.Options)
      def validated(): Either[CmtError, ListSavedStates.Options] =
        Right(options)
      end validated
  end given

  extension (options: ListSavedStates.Options)
    def execute(configuration: Configuration): Either[CmtError, String] = {
      val config = new CMTcConfig(options.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
      val MatchDotzip = ".zip".r
      val savedStates =
        sbtio
          .listFiles(config.studentifiedSavedStatesFolder)
          .to(List)
          .sorted
          .map(_.getName)
          .map(item => MatchDotzip.replaceAllIn(item, ""))
          .filter(config.exercises.contains(_))

      if (savedStates.isEmpty)
        Right(toConsoleGreen(s"No saved states found\n"))
      else
        Right(
          toConsoleGreen(s"Saved states available for exercises:\n") + toConsoleYellow(
            s"${savedStates.mkString("\n   ", "\n   ", "\n")}"))
    }

  val command = new CmtcCommand[ListSavedStates.Options] {

    def run(options: ListSavedStates.Options, args: RemainingArgs): Unit =
      args
        .enforceNoTrailingArguments()
        .flatMap(_ => options.validated().flatMap(_.execute(configuration)))
        .printResult()
  }

end ListSavedStates

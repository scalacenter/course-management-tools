package cmt.client.command

import caseapp.{AppName, CommandName, HelpMessage, Recurse, RemainingArgs}
import cmt.client.Configuration
import cmt.client.cli.SharedOptions
import cmt.client.cli.CmtcCommand
import cmt.{CMTcConfig, CmtError, printResult, toConsoleGreen, toConsoleYellow}
import cmt.core.validation.Validatable
import sbt.io.IO as sbtio
import cmt.core.cli.enforceNoTrailingArguments

object ListSavedStates:

  @AppName("list-saved-states")
  @CommandName("list-saved-states")
  @HelpMessage("List all saved exercise states, if any.")
  final case class Options(@Recurse shared: SharedOptions)

  given Validatable[ListSavedStates.Options] with
    extension (options: ListSavedStates.Options)
      def validated(): Either[CmtError, ListSavedStates.Options] =
        Right(options)
      end validated
  end given

  extension (options: ListSavedStates.Options)
    def execute(configuration: Configuration): Either[CmtError, String] = {
      val config = new CMTcConfig(options.shared.studentifiedRepo.getOrElse(configuration.currentCourse.value).value)
      val MatchDotzip = ".zip".r
      val savedStates =
        sbtio
          .listFiles(config.studentifiedSavedStatesFolder)
          .to(List)
          .sorted
          .map(_.getName)
          .map(item => MatchDotzip.replaceAllIn(item, ""))
          .filter(config.exercises.contains(_))

      Right(
        toConsoleGreen(s"Saved states available for exercises:\n") + toConsoleYellow(
          s"${savedStates.mkString("\n   ", "\n   ", "\n")}"))
    }

  val command = new CmtcCommand[ListSavedStates.Options] {

    def run(options: ListSavedStates.Options, args: RemainingArgs): Unit =
      args.enforceNoTrailingArguments().flatMap(_ => options.validated().flatMap(_.execute(configuration))).printResult()
  }

end ListSavedStates

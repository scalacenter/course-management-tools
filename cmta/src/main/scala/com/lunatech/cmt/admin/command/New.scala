package com.lunatech.cmt.admin.command

import caseapp.RemainingArgs
import com.lunatech.cmt.CmtError
import com.lunatech.cmt.core.cli.CmtCommand
import com.lunatech.cmt.core.execution.Executable
import com.lunatech.cmt.core.validation.Validatable

object New:

  final case class Options()

  given Validatable[New.Options] with
    extension (options: New.Options)
      def validated(): Either[CmtError, New.Options] =
        Right(options)
  end given

  given Executable[Delinearize.Options] with
    extension (options: Delinearize.Options)
      def execute(): Either[CmtError, String] = {
        ???
      }

  val command = new CmtCommand[New.Options] {
    def run(options: New.Options, args: RemainingArgs): Unit =
      options.validated().flatMap(_.execute()).printResult()
  }

end New

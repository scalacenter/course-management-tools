package cmt.client.cli

import caseapp.{ExtraName, Help, Parser}
import cmt.CMTcConfig
import cmt.client.Domain.StudentifiedRepo
import cmt.client.cli.ArgParsers.studentifiedRepoArgParser

final case class SharedOptions(
    @ExtraName("s")
    studentifiedRepo: StudentifiedRepo)

object SharedOptions {
  implicit val parser: Parser[SharedOptions] = Parser.derive
  implicit val help: Help[SharedOptions] = Help.derive
}

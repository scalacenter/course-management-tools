import sbt._

object Version {
  val scalaVer        = "2.11.8"
  val scalaParsers    = "1.0.3"
  val scalaTest       = "2.2.4"
  val scopt           = "3.5.0"
  val sbtio           = "0.13.12"
  val typesafeConfig  = "1.3.0"
}

object Library {
  val scalaParsers    = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.scalaParsers
  val scalaTest       = "org.scalatest"          %% "scalatest"                % Version.scalaTest
  val scopt           = "com.github.scopt"       %% "scopt"                    % Version.scopt
  val sbtio           = "org.scala-sbt"          %% "io"                       % Version.sbtio
  val typesafeConfig  = "com.typesafe"            % "config"                   % Version.typesafeConfig
}

object Dependencies {

  import Library._

  val studentify = List(
    scalaParsers,
    scopt,
    sbtio,
    typesafeConfig,
    scalaTest % "test"
  )
}

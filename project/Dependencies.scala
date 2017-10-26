import sbt._

object Version {
  val scalaVer        = "2.12.3"
  val scalaParsers    = "1.0.4"
  val scalaTest       = "3.0.1"
  val scopt           = "3.7.0"
  val sbtio           = "1.0.0"
  val typesafeConfig  = "1.3.1"
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

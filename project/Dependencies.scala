import sbt._

object Version {
  val scalaVer        = "2.13.2"
  val scalaTest       = "3.1.1"
  val scopt           = "3.7.1"
  val sbtio           = "1.3.4"
  val typesafeConfig  = "1.4.0"
}

object Library {
  val scalaTest       = "org.scalatest"          %% "scalatest"                % Version.scalaTest
  val scopt           = "com.github.scopt"       %% "scopt"                    % Version.scopt
  val sbtio           = "org.scala-sbt"          %% "io"                       % Version.sbtio
  val typesafeConfig  = "com.typesafe"            % "config"                   % Version.typesafeConfig
}

object Dependencies {

  import Library._

  val studentify = List(
    scopt,
    sbtio,
    typesafeConfig,
    scalaTest % "test"
  )
}

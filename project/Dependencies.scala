import sbt._

object Version {
  val scalaVersion    = "3.1.1"
  val scalaTest       = "3.2.11"
  val scopt           = "4.0.1"
  val sbtio           = "1.6.0"
  val typesafeConfig  = "1.4.1"
}

object Library {
  val scalaTest       = "org.scalatest"          %% "scalatest"                % Version.scalaTest
  val scopt           = "com.github.scopt"       %% "scopt"                    % Version.scopt
  val sbtio           = "org.scala-sbt"          %% "io"                       % Version.sbtio
  val typesafeConfig  = "com.typesafe"            % "config"                   % Version.typesafeConfig
}

object Dependencies {

  import Library._

  val cmtDependencies = List(
    scopt,
    sbtio,
    typesafeConfig,
    scalaTest % "test"
  )

}

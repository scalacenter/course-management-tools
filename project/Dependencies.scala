import sbt._

object Version {
  val scalaVersion    = "2.13.5"
  val scalaTest       = "3.1.1"
  val scopt           = "3.7.1"
  val sbtio           = "1.4.0"
  val typesafeConfig  = "1.4.1"
  val requestsVersion = "0.6.5"
  val uJson           = "0.9.5"
}

object Library {
  val scalaTest       = "org.scalatest"          %% "scalatest"                % Version.scalaTest
  val scopt           = "com.github.scopt"       %% "scopt"                    % Version.scopt
  val sbtio           = "org.scala-sbt"          %% "io"                       % Version.sbtio
  val typesafeConfig  = "com.typesafe"            % "config"                   % Version.typesafeConfig
  val requestsLib     = "com.lihaoyi"            %% "requests"                 % Version.requestsVersion
  val uJson           = "com.lihaoyi"            %% "ujson"                    % Version.uJson
}

object Dependencies {

  import Library._

  val cmtDependencies = List(
    scopt,
    sbtio,
    typesafeConfig,
    scalaTest % "test"
  )

  val mainAdmDependencies = List(
    requestsLib,
    uJson
  )
}

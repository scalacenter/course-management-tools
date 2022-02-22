import sbt._
import sbt.Keys._

object CompileOptions {
  val compileOptions = Seq(
    "-source:future"
  )
}

object CommonSettings {
  lazy val commonSettings = Seq(
    organization := "com.github.eloots",
    version := "2.0.0-SNAPSHOT",
    scalaVersion := Version.scalaVersion,
    scalacOptions ++= CompileOptions.compileOptions,
    Test / parallelExecution := false,
    Test / logBuffered := false,
    // ThisBuild / parallelExecution := false,
    libraryDependencies ++= Dependencies.cmtDependencies
  )
}

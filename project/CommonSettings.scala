import com.typesafe.sbteclipse.core.EclipsePlugin.{EclipseCreateSrc, EclipseKeys}
import sbt.Keys._
import sbt._

object CompileOptions {
  val compileOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-Xlint:_",
    "-encoding", "UTF-8"
  )
}

object CommonSettings {
  lazy val commonSettings = Seq(
    organization := "com.typesafe.training",
    version := "1.0.0",
    scalaVersion := Version.scalaVer,
    scalacOptions ++= CompileOptions.compileOptions,
    unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
    unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
    EclipseKeys.createSrc := EclipseCreateSrc.Default,
    EclipseKeys.eclipseOutput := Some(".target"),
    EclipseKeys.withSource := true,
    EclipseKeys.skipParents in ThisBuild := true,
    EclipseKeys.skipProject := true,
    parallelExecution in Test := false,
    logBuffered in Test := false,
    parallelExecution in ThisBuild := false,
    libraryDependencies ++= Dependencies.studentify
  )
}

import sbt._
import sbt.Keys._
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys._

object Build {

  object CompileOptions {
    val compileOptions = Seq("-source:future", "-deprecation")
  }

  lazy val commonSettings = Seq(
    organization := "com.github.eloots",
    version := "2.0.0-SNAPSHOT",
    scalaVersion := Version.scalaVersion,
    scalacOptions ++= CompileOptions.compileOptions,
    buildInfoPackage := "cmt.version",
    Test / parallelExecution := false,
    Test / logBuffered := false,
    // ThisBuild / parallelExecution := false,
    libraryDependencies ++= Dependencies.cmtDependencies)

  lazy val commonBuildInfoKeys = Seq[BuildInfoKey](version, scalaVersion, sbtVersion)

  def buildKeysWithName(projectName: String): Seq[BuildInfoKey] =
    BuildInfoKey.map(name) { case (k, _) =>
      k -> projectName
    } +: commonBuildInfoKeys
}

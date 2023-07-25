import sbt._
import sbt.Keys._
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys._

object Build {

  object CompileOptions {
    val compileOptions =
      Seq(
        "-source:future",
        "-deprecation",
        "-Wunused:imports",
        "-Wunused:locals",
        "-Wunused:privates",
        "-Wunused:explicits",
        "-Wunused:implicits",
        "-Wunused:params",
        "-Wvalue-discard")
  }

  lazy val commonSettings = Seq(
    organization := "com.github.lunatech-labs",
    scalaVersion := Version.scalaVersion,
    scalacOptions ++= CompileOptions.compileOptions,
    buildInfoPackage := "com.lunatech.cmt.version",
    Test / parallelExecution := false,
    Test / logBuffered := false)

  lazy val commonBuildInfoKeys = Seq[BuildInfoKey](version, scalaVersion, sbtVersion)

  def buildKeysWithName(projectName: String): Seq[BuildInfoKey] =
    BuildInfoKey.map(name) { case (k, _) =>
      k -> projectName
    } +: commonBuildInfoKeys
}

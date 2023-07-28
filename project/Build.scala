import sbt._
import sbt.Keys._
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys._
import sbtnativeimage.NativeImagePlugin.autoImport.nativeImageJvm
import sbtnativeimage.NativeImagePlugin.autoImport.nativeImageOptions
import sbtnativeimage.NativeImagePlugin.autoImport.nativeImageVersion

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
    scalaVersion := Version.scalaVersion,
    scalacOptions ++= CompileOptions.compileOptions,
    buildInfoPackage := "com.lunatech.cmt.version",
    Test / parallelExecution := false,
    Test / logBuffered := false)

  lazy val nativeImageSettings =
    Seq(
      nativeImageJvm := "graalvm-java17",
      nativeImageVersion := "22.3.1",
      nativeImageOptions := Seq("--no-fallback")
    )

  lazy val commonBuildInfoKeys = Seq[BuildInfoKey](version, scalaVersion, sbtVersion)

  def buildKeysWithName(projectName: String): Seq[BuildInfoKey] =
    BuildInfoKey.map(name) { case (k, _) =>
      k -> projectName
    } +: commonBuildInfoKeys
}

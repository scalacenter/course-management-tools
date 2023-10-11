import Build.*

inThisBuild(
  List(
    organization := "ch.epfl.scala",
    organizationName := "Scala Center",
    organizationHomepage := Some(url("https://scala.epfl.ch")),
    homepage := Some(url("https://scala.epfl.ch")),
    developers := List(Developer("eloots", "Eric Loots", "eric.loots@lunatech.com", url("https://github.com/eloots"))),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/license/LICENSE-2.0"))))

ThisBuild / dynverVTagPrefix := false
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

lazy val `course-management-tools` =
  (project in file("."))
    .aggregate(cmta, cmtc, `cmt-core`, `functional-tests`, docs)
    .settings(commonSettings: _*)
    .settings(publish / skip := true)

lazy val `cmt-core` =
  project
    .in(file("cmt-core"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Dependencies.coreDependencies)

lazy val cmta = project
  .in(file("cmta"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(`cmt-core`, `cmt-core` % "test->test")
  .settings(commonSettings: _*)
  .settings(nativeImageSettings: _*)
  .settings(Compile / mainClass := Some("coursemgmttools.admin.Main"))
  .settings(buildInfoKeys := buildKeysWithName("cmta:Course Management Tools (Admin)"))

lazy val cmtc = project
  .in(file("cmtc"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(`cmt-core`, `cmt-core` % "test->test")
  .settings(commonSettings: _*)
  .settings(nativeImageSettings: _*)
  .settings(libraryDependencies ++= Dependencies.cmtcDependencies)
  .settings(Compile / mainClass := Some("coursemgmttools.client.Main"))
  .settings(buildInfoKeys := buildKeysWithName("Course Management Tools (Client)"))

lazy val `functional-tests` = project
  .in(file("functional-tests"))
  .dependsOn(cmta, cmtc, `cmt-core`)
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.functionalTestDependencies)
  .settings(publish / skip := true)

lazy val docs = project
  .in(file("course-management-tools-docs"))
  .settings(moduleName := "course-management-tools-docs", publish / skip := true)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

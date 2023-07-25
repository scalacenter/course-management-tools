import Build._

ThisBuild / dynverVTagPrefix := false
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

lazy val `course-management-tools` =
  (project in file("."))
    .aggregate(cmta, cmtc, core, `functional-tests`, docs)
    .settings(commonSettings: _*)
    .settings(publish / skip := true)

lazy val core =
  project.in(file("core")).settings(commonSettings: _*).settings(libraryDependencies ++= Dependencies.coreDependencies)

lazy val cmta = project
  .in(file("cmta"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core, core % "test->test")
  .settings(commonSettings: _*)
  .settings(Compile / mainClass := Some("com.lunatech.cmt.admin.Main"))
  .settings(buildInfoKeys := buildKeysWithName("cmta:Course Management Tools (Admin)"))

lazy val cmtc = project
  .in(file("cmtc"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core, core % "test->test")
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.cmtcDependencies)
  .settings(Compile / mainClass := Some("com.lunatech.cmt.client.Main"))
  .settings(buildInfoKeys := buildKeysWithName("Course Management Tools (Client)"))

lazy val `functional-tests` = project
  .in(file("functional-tests"))
  .dependsOn(cmta, cmtc, core)
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.functionalTestDependencies)
  .settings(publish / skip := true)

lazy val docs = project
  .in(file("course-management-tools-docs"))
  .settings(moduleName := "course-management-tools-docs", publish / skip := true)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

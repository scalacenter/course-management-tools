import Build._

lazy val `course-management-tools` =
  (project in file("."))
    .aggregate(cmta, cmtc, core, `functional-tests`, docs)
    .settings(commonSettings: _*)
    .settings(publish / skip := true)

lazy val core = project.in(file("core")).settings(commonSettings: _*)

lazy val cmta = project
  .in(file("cmta"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core, core % "test->test")
  .settings(commonSettings: _*)
  .settings(Compile / mainClass := Some("cmt.admin.Main"))
  .settings(buildInfoKeys := buildKeysWithName("Course Management Tools (Admin)"))

lazy val cmtc = project
  .in(file("cmtc"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core, core % "test->test")
  .settings(commonSettings: _*)
  .settings(Compile / mainClass := Some("cmt.client.Main"))
  .settings(buildInfoKeys := buildKeysWithName("Course Management Tools (Client)"))

lazy val `functional-tests` = project.in(file("functional-tests"))
  .dependsOn(cmta, cmtc % "compile->compile;test->test")
  .settings(commonSettings: _*)

lazy val docs = project
  .in(file("course-management-tools-docs"))
  .settings(
    moduleName := "course-management-tools-docs",
    skip.in(publish) := true
  )
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

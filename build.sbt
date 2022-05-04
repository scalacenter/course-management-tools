lazy val `course-management-tools` =
  (project in file("."))
    .aggregate(
      cmt,
      core,
    )
    .settings(CommonSettings.commonSettings: _*)
    .settings(publish / skip := true)

lazy val core = project
  .in(file("core"))
  .settings(CommonSettings.commonSettings: _*)

lazy val cmt = project
  .in(file("cmt"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)


lazy val `course-management-tools` =
  (project in file("."))
    .aggregate(
      cmta,
      cmtc,
      core
    )
    .settings(CommonSettings.commonSettings: _*)
    .settings(publish / skip := true)

lazy val core = project
  .in(file("core"))
  .settings(CommonSettings.commonSettings: _*)

lazy val cmta = project
  .in(file("cmta"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)

lazy val cmtc = project
  .in(file("cmtc"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)

lazy val `course-management-tools` =
  (project in file("."))
    .aggregate(
      core,
      studentify,
      linearize,
      delinearize,
      mainadm
    )
    .settings(ThisBuild / scalaVersion := Version.scalaVersion)

lazy val core = project
  .in(file("core"))
  .settings(CommonSettings.commonSettings: _*)

lazy val studentify = project
  .in(file("studentify"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)

lazy val linearize = project
  .in(file("linearize"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)

lazy val delinearize = project
  .in(file("delinearize"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)

lazy val mainadm = project
  .in(file("mainadm"))
  .dependsOn(core)
  .settings(CommonSettings.commonSettings: _*)

addCommandAlias("studentify", "studentify/run")
addCommandAlias("linearize", "linearize/run")
addCommandAlias("delinearize", "delinearize/run")
addCommandAlias("mainadm", "mainadm/run")

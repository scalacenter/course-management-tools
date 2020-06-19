organization := "com.lightbend.studentify"

name := "studentify"

version := "1.0.0"

scalaVersion := Version.scalaVer

// The Typesafe repository
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.url("Typesafe Ivy Releases", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

lazy val `course-management-tools` =
  (project in file("."))
  .settings(CommonSettings.commonSettings: _*)

addCommandAlias("studentify", "runMain com.lightbend.coursegentools.Studentify")
addCommandAlias("linearize", "runMain com.lightbend.coursegentools.Linearize")
addCommandAlias("delinearize", "runMain com.lightbend.coursegentools.DeLinearize")
addCommandAlias("mainadm", "runMain com.lightbend.coursegentools.MainAdm")

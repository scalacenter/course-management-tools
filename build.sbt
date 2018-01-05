organization := "com.lightbend.studentify"

name := "studentify"

version := "1.0.0"

scalaVersion := Version.scalaVer

// The Typesafe repository
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.url("Typesafe Ivy Releases", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Dependencies.studentify

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

addCommandAlias("studentify", "runMain com.lightbend.coursegentools.Studentify")
addCommandAlias("linearize", "runMain com.lightbend.coursegentools.Linearize")
addCommandAlias("delinearize", "runMain com.lightbend.coursegentools.DeLinearize")
addCommandAlias("masteradm", "runMain com.lightbend.coursegentools.MasterAdm")

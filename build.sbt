organization := "com.lightbend.studentify"

name := "studentify"

version := "1.0.0"

scalaVersion := Version.scalaVer

// The Typesafe repository
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Dependencies.studentify

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)


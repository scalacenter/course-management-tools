/***************************************************************
  *      THIS IS A GENERATED FILE - EDIT AT YOUR OWN RISK      *
  **************************************************************
  *
  * Use the mainadm command to generate a new version of
  * this build file.
  *
  * See https://github.com/lightbend/course-management-tools
  * for more details
  *
  */

import sbt._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `play-scala-course-root` = (project in file("."))
  .aggregate(
    `exercise_000_initial_state`,
    `exercise_001_http_routes`
  )
  .settings(CommonSettings.commonSettings: _*)

lazy val `exercise_000_initial_state` = project
  .configure(CommonSettings.configure)
   .enablePlugins(PlayScala)
   .settings(libraryDependencies += guice)

lazy val `exercise_001_http_routes` = project
  .configure(CommonSettings.configure)
   .enablePlugins(PlayScala)
   .settings(libraryDependencies += guice)
       
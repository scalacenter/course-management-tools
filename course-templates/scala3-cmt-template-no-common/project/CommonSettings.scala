import sbt.Keys._
import sbt._
import sbtstudent.AdditionalSettings

object CommonSettings {
  lazy val commonSettings = Seq(
    Compile / scalacOptions ++= CompileOptions.compileOptions,
    Compile / javacOptions ++= Seq("--release", "8"),
    Compile / unmanagedSourceDirectories := List((Compile / scalaSource).value, (Compile / javaSource).value),
    Test / unmanagedSourceDirectories := List((Test / scalaSource).value, (Test / javaSource).value),
    Test / logBuffered := false,
    Test / parallelExecution := false,
    libraryDependencies ++= Dependencies.dependencies,
    libraryDependencies ++= Dependencies.crossDependencies.map(_.cross(CrossVersion.for3Use2_13)),
  ) ++
    AdditionalSettings.initialCmdsConsole ++
    AdditionalSettings.initialCmdsTestConsole ++
    AdditionalSettings.cmdAliases

  lazy val configure: Project => Project = (project: Project) => {
    project.settings(CommonSettings.commonSettings: _*)
  }
}

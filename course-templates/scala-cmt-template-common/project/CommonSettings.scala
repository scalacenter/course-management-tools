import sbt.Keys._
import sbt._
import sbtstudent.AdditionalSettings

object CommonSettings {
  lazy val commonSettings = Seq(
    Compile / scalacOptions ++= CompileOptions.compileOptions,
    Compile / javacOptions ++= Seq("--release", "8"),
    Compile / unmanagedSourceDirectories := List((scalaSource in Compile).value, (javaSource in Compile).value),
    Test / unmanagedSourceDirectories := List((scalaSource in Test).value, (javaSource in Test).value),
    Test / logBuffered := false,
    Test / parallelExecution := false,
    libraryDependencies ++= Dependencies.dependencies,
    testFrameworks += new TestFramework("munit.Framework")
  ) ++
    AdditionalSettings.initialCmdsConsole ++
    AdditionalSettings.initialCmdsTestConsole ++
    AdditionalSettings.cmdAliases

  lazy val configure: Project => Project = (project: Project) => {
    project.settings(CommonSettings.commonSettings: _*)
  }
}

import sbt.Keys._
import sbt._
import sbtstudent.AdditionalSettings
import sbtstudent.StudentCommandsPlugin._

object CommonSettings {
  lazy val commonSettings = Seq(
    Compile / scalacOptions ++= CompileOptions.compileOptions,
    Compile / unmanagedSourceDirectories := List((scalaSource in Compile).value, (javaSource in Compile).value),
    Test / unmanagedSourceDirectories := List((scalaSource in Test).value, (javaSource in Test).value),
    Test / logBuffered := false,
    Test / parallelExecution := false,
    libraryDependencies ++= Dependencies.dependencies,
    shellPrompt := (state => renderCMTPrompt(state))
  ) ++
    AdditionalSettings.initialCmdsConsole ++
    AdditionalSettings.initialCmdsTestConsole ++
    AdditionalSettings.cmdAliases

  lazy val configure: Project => Project = (project: Project) => {
    project.settings(CommonSettings.commonSettings: _*)
  }
}

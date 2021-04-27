import sbt.Keys._
import sbt._
import sbtstudent.AdditionalSettings
import sbtstudent.StudentCommandsPlugin._

object CommonSettings {
  lazy val commonSettings = Seq(
    Compile / scalacOptions ++= CompileOptions.compileOptions,
    Compile / unmanagedSourceDirectories := List((Compile / scalaSource).value, (Compile / javaSource).value),
    Test / unmanagedSourceDirectories := List((Test / scalaSource).value, (Test / javaSource).value),
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

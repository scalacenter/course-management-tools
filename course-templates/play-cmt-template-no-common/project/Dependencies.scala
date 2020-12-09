import sbt._

object Version {
  val logbackVer        = "1.2.3"
  val scalaVersion      = "2.13.4"
}

object Dependencies {

  private val logbackDeps = Seq (
    "ch.qos.logback"                 %  "logback-classic"
  ).map (_ % Version.logbackVer)

  private val testDeps = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
  )

  val dependencies: Seq[ModuleID] =
    logbackDeps ++
    testDeps
}

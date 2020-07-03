import sbt._

object Version {
  val logbackVer        = "1.2.3"
  val mUnitVer          = "0.7.9"
  val scalaVersion      = "2.13.2"
}

object Dependencies {

  private val logbackDeps = Seq (
    "ch.qos.logback"                 %  "logback-classic",
  ).map (_ % Version.logbackVer)

  private val munitDeps = Seq(
    "org.scalameta" %% "munit" % Version.mUnitVer % Test
  )

  val dependencies: Seq[ModuleID] =
    logbackDeps ++
    munitDeps
}

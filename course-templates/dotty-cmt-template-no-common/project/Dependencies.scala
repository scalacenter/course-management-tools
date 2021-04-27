import sbt._

object Version {
  val logbackVer        = "1.2.3"
  val mUnitVer          = "0.7.25"
  val scalaVersion      = "3.0.0-RC3"
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

  val crossDependencies: Seq[ModuleID] =
    Seq.empty
}

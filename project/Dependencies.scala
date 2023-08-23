import sbt._

object Version {
  lazy val scalaVersion = "3.3.0"
  lazy val scalaTestVersion = "3.2.16"
  lazy val scalaCheckVersion = "3.2.14.0"
  lazy val sbtioVersion = "1.9.1"
  lazy val typesafeConfigVersion = "1.4.2"
  lazy val caseappVersion = "2.1.0-M25"
  lazy val catsVersion = "2.10.0"
  lazy val devDirsVersion = "26"
  lazy val github4sVersion = "0.32.0"
  lazy val http4sVersion = "0.23.15"
  lazy val circeVersion = "0.14.5"
  // lazy val circeConfig = "0.8.0"
  lazy val LiHaoyiOsLibVersion = "0.9.1"
  lazy val LiHaoyiRequestLibVersion = "0.8.0"
  lazy val commonsCodecVersion = "1.16.0"

}

object Library {

  import Version._

  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  lazy val scalaCheck = "org.scalatestplus" %% "scalacheck-1-16" % scalaCheckVersion % Test
  lazy val sbtio = "org.scala-sbt" %% "io" % sbtioVersion
  lazy val typesafeConfig = "com.typesafe" % "config" % typesafeConfigVersion
  lazy val commonsCodec = "commons-codec" % "commons-codec" % commonsCodecVersion
  lazy val caseapp = "com.github.alexarchambault" %% "case-app" % caseappVersion
  lazy val cats = "org.typelevel" %% "cats-core" % catsVersion
  lazy val devDirs = ("dev.dirs" % "directories" % devDirsVersion).withJavadoc()
  lazy val requestLib = "com.lihaoyi" %% "requests" % LiHaoyiRequestLibVersion
  lazy val osLib = "com.lihaoyi" %% "os-lib" % LiHaoyiOsLibVersion

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe").map(_ % http4sVersion)

  lazy val circe = "io.circe" %% "circe-generic" % circeVersion
  lazy val github4s = "com.47deg" %% "github4s" % github4sVersion
}

object Dependencies {

  import Library._

  lazy val coreDependencies = (http4s ++ List(
    sbtio,
    typesafeConfig,
    scalaTest,
    scalaCheck,
    commonsCodec,
    caseapp,
    cats,
    devDirs,
    circe,
    github4s)).map(_.withSources())
  lazy val cmtDependencies =
    List(sbtio, typesafeConfig, scalaTest, scalaCheck, commonsCodec, caseapp, cats).map(_.withSources())
  lazy val cmtcDependencies =
    (http4s ++ List(devDirs, circe, github4s, scalaTest, scalaCheck, osLib, requestLib)).map(_.withSources())
  lazy val functionalTestDependencies = List(scalaTest, scalaCheck).map(_.withSources())
}

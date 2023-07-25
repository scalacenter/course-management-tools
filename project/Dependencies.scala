import sbt._

object Version {
  lazy val scalaVersion = "3.3.0"
  lazy val scalaTest = "3.2.16"
  lazy val scalaCheck = "3.2.9.0"
  lazy val sbtio = "1.9.1"
  lazy val typesafeConfig = "1.4.2"
  lazy val caseapp = "2.1.0-M25"
  lazy val cats = "2.8.0"
  lazy val devDirs = "26"
  lazy val github4s = "0.32.0"
  lazy val http4s = "0.23.15"
  lazy val circe = "0.14.5"
  lazy val circeConfig = "0.8.0"
}

object Library {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % Test
  lazy val scalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaCheck % Test
  lazy val sbtio = "org.scala-sbt" %% "io" % Version.sbtio
  lazy val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig
  lazy val commonsCodec = "commons-codec" % "commons-codec" % "1.16.0"
  lazy val caseapp = "com.github.alexarchambault" %% "case-app" % Version.caseapp
  lazy val cats = "org.typelevel" %% "cats-core" % Version.cats
  lazy val devDirs = ("dev.dirs" % "directories" % Version.devDirs).withJavadoc()

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe").map(_ % Version.http4s)

  lazy val circe = "io.circe" %% "circe-generic" % Version.circe
  lazy val github4s = "com.47deg" %% "github4s" % Version.github4s
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
  lazy val cmtcDependencies = (http4s ++ List(devDirs, circe, github4s, scalaTest, scalaCheck)).map(_.withSources())
  lazy val functionalTestDependencies = List(scalaTest, scalaCheck).map(_.withSources())
}

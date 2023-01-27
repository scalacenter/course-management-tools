import sbt._

object Version {
  val scalaVersion = "3.3.0-RC1"
  val scalaTest = "3.2.14"
  val scalaCheck = "3.2.9.0"
  val sbtio = "1.7.0"
  val typesafeConfig = "1.4.2"
  val caseapp = "2.1.0-M21"
  val cats = "2.8.0"
}

object Library {
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % Test
  val scalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaCheck % Test
  val sbtio = "org.scala-sbt" %% "io" % Version.sbtio
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig
  val commonsCodec = "commons-codec" % "commons-codec" % "1.15"
  val caseapp = "com.github.alexarchambault" %% "case-app" % Version.caseapp
  val cats = "org.typelevel" %% "cats-core" % Version.cats
}

object Dependencies {

  import Library._

  val cmtDependencies = List(sbtio, typesafeConfig, scalaTest, scalaCheck, commonsCodec, caseapp, cats)

}

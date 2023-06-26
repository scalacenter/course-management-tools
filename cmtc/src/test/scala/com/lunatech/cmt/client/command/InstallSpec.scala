package com.lunatech.cmt.client.command

import cats.effect.IO
import github4s.Github
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration.*

import scala.concurrent.Await

final class InstallSpec extends AnyWordSpecLike with Matchers {

  "github4s" should {
    "do something" in {
      val accessToken = Some("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")
      val httpClient: Client[IO] = {
        JavaNetClientBuilder[IO].create // You can use any http4s backend
      }
//      val github = Github[IO](httpClient, accessToken)
      val user1 = Github[IO](httpClient, accessToken).repos.latestRelease(
        "lunatech-labs",
        "lunatech-scala-2-to-scala3-course").unsafeToFuture()
      Await.result(user1, 10.seconds)
      println(user1)
    }
  }
}

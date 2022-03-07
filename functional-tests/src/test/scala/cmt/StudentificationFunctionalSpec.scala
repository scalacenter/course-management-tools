package cmt

import cmt.support.CMTSupport
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

final class StudentificationFunctionalSpec extends AnyFeatureSpecLike with Matchers with GivenWhenThen with CMTSupport {

  info("As a user")
  info("I want to be able to studentify a main repository")
  info("So I can share a course")

  Feature("Studentification") {

    Scenario("A user studentifies a main repository") {
      Given("a main repository")
      When("the main repository is studentified")
      Then("the result is a studentified repository")
      pending
    }
  }
}

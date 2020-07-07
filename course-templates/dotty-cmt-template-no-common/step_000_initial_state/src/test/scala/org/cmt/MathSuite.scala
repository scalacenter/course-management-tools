package org.cmt

class MathSuite extends munit.FunSuite {

  test("Adding 0 to any integer value should return the same value") {
    import Math._

    for {
      i <- 1 to 100
    } assertEquals(add(i, 0), i)
  }

  test("Verify basic addition") {
    import Math._

    assertEquals(add(100, -5), 95)
  }
}

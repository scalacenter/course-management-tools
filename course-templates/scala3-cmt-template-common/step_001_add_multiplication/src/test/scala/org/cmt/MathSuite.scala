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

  test("Multiplying any integer value by 1 should return the same value") {
    import Math._

    for {
      i <- 1 to 100
    } assertEquals(multiply(i, 1), i)
  }

  test("Verify basic multiplication") {
    import Math._

    assertEquals(multiply(100, 0), 0)
    assertEquals(multiply(100, -1), -100)
    assertEquals(multiply(13, 14), 182)
  }
}

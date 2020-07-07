package org.cmt

@main def main2(): Unit = {

  println("Hello world!")

  val a = 5
  val b = 7
  println(s"$a + $b = ${Math.add(a, b)}")
  println(s"$a x $b = ${Math.multiply(a, b)}")
}

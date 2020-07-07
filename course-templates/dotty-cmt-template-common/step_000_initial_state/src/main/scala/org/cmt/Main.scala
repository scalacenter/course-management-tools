package org.cmt

@main def main(): Unit = {

  println("Hello world!")

  val a = 5
  val b = 7
  println(s"$a + $b = ${Math.add(a, b)}")
  println(s"A common value = ${Common.aCommonValue}")
}

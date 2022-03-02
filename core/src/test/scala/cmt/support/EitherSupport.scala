package cmt.support

import scopt.OEffect.ReportError

trait EitherSupport {

  def assertRight[E, T](either: Either[E, T]): T =
    either match {
      case Left(error)   => throw new IllegalStateException(s"Expected Either.right, got Either.left [$error]")
      case Right(result) => result
    }

  def assertLeft[E, T](either: Either[E, T]): E =
    either match {
      case Left(error)   => error
      case Right(result) => throw new IllegalStateException(s"Expected Either.left, got Either.right [$result]")
    }
}

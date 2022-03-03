package cmt.core.execution

trait Executable[T]:
  extension (t: T) def execute(): Either[String, String]

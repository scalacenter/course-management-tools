package cmt.core.validation

import cmt.CmtError

trait Validatable[T]:
  extension (t: T) def validated(): Either[CmtError, T]

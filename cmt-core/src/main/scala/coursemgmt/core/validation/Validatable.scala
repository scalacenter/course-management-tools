package coursemgmt.core.validation

import coursemgmt.CmtError

trait Validatable[T]:
  extension (t: T) def validated(): Either[CmtError, T]

package coursemgmttools.core.validation

import coursemgmttools.CmtError

trait Validatable[T]:
  extension (t: T) def validated(): Either[CmtError, T]

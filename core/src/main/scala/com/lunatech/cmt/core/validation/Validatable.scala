package com.lunatech.cmt.core.validation

import com.lunatech.cmt.CmtError

trait Validatable[T]:
  extension (t: T) def validated(): Either[CmtError, T]

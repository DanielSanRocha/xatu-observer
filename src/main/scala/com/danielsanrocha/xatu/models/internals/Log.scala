package com.danielsanrocha.xatu.models.internals

object Log {
  type Log = Either[LogService, LogContainer]
}

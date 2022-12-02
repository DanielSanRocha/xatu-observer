package com.danielsanrocha.xatu.models.internals

case class LogServiceObserverStatus(
    id: Long,
    name: String,
    files: Seq[String]
) extends Status

package com.danielsanrocha.xatu.models.internals

case class NewService(
    name: String,
    logFileDirectory: String,
    logFileRegex: String,
    pidFile: String
)

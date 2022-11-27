package com.danielsanrocha.xatu.models.internals

import java.sql.Timestamp

case class Service(
    id: Long,
    name: String,
    logFileDirectory: String,
    logFileRegex: String,
    pidFile: String,
    createDate: Timestamp,
    updateDate: Timestamp
)

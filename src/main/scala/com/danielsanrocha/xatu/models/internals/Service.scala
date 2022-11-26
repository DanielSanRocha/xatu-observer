package com.danielsanrocha.xatu.models.internals

import java.sql.Timestamp

case class Service(
    id: Long,
    name: String,
    logfileRegex: String,
    pidfile: String,
    createDate: Timestamp,
    updateDate: Timestamp
)

package com.danielsanrocha.xatu.models.internals

import java.sql.Timestamp

case class API(
    id: Long,
    name: String,
    host: String,
    port: Int,
    healthcheckRoute: String,
    status: Char,
    createDate: Timestamp,
    updateDate: Timestamp
)

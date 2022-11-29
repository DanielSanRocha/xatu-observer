package com.danielsanrocha.xatu.models.internals

import java.sql.Timestamp

case class Container(
    id: Long,
    name: String,
    createDate: Timestamp,
    updateDate: Timestamp
)

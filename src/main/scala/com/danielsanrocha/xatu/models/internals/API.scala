package com.danielsanrocha.xatu.models.internals

import java.sql.Timestamp

case class API
(
  id: Long,
  name: String,
  host: String,
  port: Int,
  createDate: Timestamp,
  updateDate: Timestamp
)

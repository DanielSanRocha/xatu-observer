package com.danielsanrocha.xatu.models.requests

import com.twitter.finatra.http.annotations.RouteParam

case class ServiceRequest
(
  @RouteParam id: Long,
  name: String,
  logFileDirectory: String,
  logFileRegex: String,
  pidFile: String
)

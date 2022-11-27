package com.danielsanrocha.xatu.models.requests

import com.twitter.finatra.http.annotations.RouteParam

final case class Id
(
  @RouteParam id: Long
)

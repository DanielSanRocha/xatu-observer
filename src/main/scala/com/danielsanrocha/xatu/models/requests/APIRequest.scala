package com.danielsanrocha.xatu.models.requests

import com.twitter.finatra.http.annotations.RouteParam

case class APIRequest(
    @RouteParam id: Long,
    name: String,
    host: String,
    port: Int,
    healthcheckRoute: String
)

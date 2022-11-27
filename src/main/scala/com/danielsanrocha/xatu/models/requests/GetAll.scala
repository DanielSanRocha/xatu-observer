package com.danielsanrocha.xatu.models.requests

import com.twitter.finatra.http.annotations.QueryParam

case class GetAll(
    @QueryParam limit: Long,
    @QueryParam offset: Long
)

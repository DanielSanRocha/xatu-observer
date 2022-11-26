package com.danielsanrocha.xatu.models.internals

import com.twitter.finagle.context.Contexts

final case class RequestId(
    requestId: String
)

object RequestId extends Contexts.local.Key[RequestId]

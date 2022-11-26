package com.danielsanrocha.xatu.models.internals

import com.twitter.finagle.context.Contexts

final case class TimedCredential(
    id: Long,
    email: String,
    timestamp: Long
)

object TimedCredential extends Contexts.local.Key[TimedCredential]

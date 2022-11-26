package com.danielsanrocha.xatu.models.responses

final case class Token(
    message: String,
    token: String,
    requestId: String
)

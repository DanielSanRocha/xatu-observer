package com.danielsanrocha.xatu.models.responses

import com.fasterxml.jackson.annotation.JsonProperty

final case class ServerMessage(
    message: String,
    @JsonProperty("request_id") requestId: String
)

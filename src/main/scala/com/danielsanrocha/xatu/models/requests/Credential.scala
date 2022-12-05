package com.danielsanrocha.xatu.models.requests

import com.fasterxml.jackson.annotation.JsonProperty

case class Credential(
    @JsonProperty("email") email: String,
    @JsonProperty("password") password: String
)

package com.danielsanrocha.xatu.models.internals

import com.fasterxml.jackson.annotation.JsonProperty

case class LogContainer(
    @JsonProperty("container_id") containerId: Long,
    @JsonProperty("container_name") containerName: String,
    message: String,
    @JsonProperty("created_at") createdAt: Long
)

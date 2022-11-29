package com.danielsanrocha.xatu.models.internals

import com.fasterxml.jackson.annotation.JsonProperty

case class ContainerInfo(
    @JsonProperty("image_name") imageName: String,
    @JsonProperty("container_id") containerId: String
)

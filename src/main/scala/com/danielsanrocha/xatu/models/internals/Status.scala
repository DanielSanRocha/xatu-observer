package com.danielsanrocha.xatu.models.internals

import com.fasterxml.jackson.annotation.JsonProperty

case class Status(
    @JsonProperty("api_observer_manager") val apiObserverManager: Map[Long, String],
    @JsonProperty("log_service_observer_manager") val logServiceObserverManager: Map[Long, String],
    @JsonProperty("service_observer_manager") val serviceObserverManager: Map[Long, String],
    @JsonProperty("log_container_manager") val logContainerManager: Map[Long, String]
)

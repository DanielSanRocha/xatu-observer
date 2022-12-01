package com.danielsanrocha.xatu.models.internals

import scala.collection.mutable
import com.fasterxml.jackson.annotation.JsonProperty

case class Status(
    @JsonProperty("api_observer_manager") val apiObserverManager: mutable.Map[Long, String],
    @JsonProperty("log_service_observer_manager") val logServiceObserverManager: mutable.Map[Long, String],
    @JsonProperty("service_observer_manager") val serviceObserverManager: mutable.Map[Long, String],
    @JsonProperty("log_container_manager") val logContainerManager: mutable.Map[Long, String]
)

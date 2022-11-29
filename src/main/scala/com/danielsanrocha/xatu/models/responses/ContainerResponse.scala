package com.danielsanrocha.xatu.models.responses

import com.danielsanrocha.xatu.models.internals.{ContainerInfo}

case class ContainerResponse(
    id: Long,
    name: String,
    info: Option[ContainerInfo],
    status: Char
)

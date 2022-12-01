package com.danielsanrocha.xatu.models.responses

import java.sql.Timestamp
import com.danielsanrocha.xatu.models.internals.{Container, ContainerInfo, Data}

case class ContainerResponse(
    override val id: Long,
    override val name: String,
    info: Option[ContainerInfo],
    status: Char,
    createDate: Timestamp,
    updateDate: Timestamp
) extends Data {
  def container: Container = Container(id, name, createDate, updateDate)
}

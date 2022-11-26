package com.danielsanrocha.xatu.models.responses

import java.sql.Timestamp
import com.fasterxml.jackson.annotation.JsonProperty

import com.danielsanrocha.xatu.models.internals.User

case class UserResponse(
    id: Long,
    name: String,
    email: String,
    @JsonProperty("create_date") createDate: Timestamp,
    @JsonProperty("update_date") updateDate: Timestamp
)

object UserResponse {
  def apply(user: User): UserResponse =
    UserResponse(id = user.id, name = user.name, email = user.email, createDate = user.createDate, updateDate = user.updateDate)

}

package com.danielsanrocha.xatu.models.internals

class Data(val id: Long, val name: String) {
  def apply(id: Long, name: String) = new Data(id, name)
}

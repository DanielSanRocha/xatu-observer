package com.danielsanrocha.xatu.commons

object Conversion {
  def toLong(s: String): Option[Long] = {
    try {
      Some(s.toLong)
    } catch {
      case e: Exception => None
    }
  }
}

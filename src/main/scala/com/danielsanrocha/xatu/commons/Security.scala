package com.danielsanrocha.xatu.commons

import java.util.Base64
import java.security.MessageDigest

object Security {
  def hash(s: String) = {
    val bytes = MessageDigest.getInstance("MD5").digest(s.getBytes)
    Base64.getEncoder().withoutPadding().encodeToString(bytes)
  }
}

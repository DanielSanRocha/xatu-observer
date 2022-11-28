package com.danielsanrocha.xatu.commons

import scala.util.matching.Regex

object RegexUtils {
  implicit class RichRegex(val underlying: Regex) extends AnyVal {
    def matches(s: String) = underlying.pattern.matcher(s).matches
  }
}

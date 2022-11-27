package com.danielsanrocha.xatu.models.responses

case class HitsResult[T](
    count: Long,
    hits: Seq[T]
)

package com.danielsanrocha.xatu.filters

import scala.language.postfixOps
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Duration, Future => TwitterFuture}

import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import com.danielsanrocha.xatu.commons.FutureConverters._
import com.danielsanrocha.xatu.exceptions.TimeoutException
import com.twitter.finagle.http.{Request, Response}
import com.typesafe.scalalogging.Logger

class TimeoutFilter(timeout: Duration, implicit val ec: ExecutionContext) extends SimpleFilter[Request, Response] {
  private val logging: Logger = Logger(this.getClass)

  def apply(request: Request, service: Service[Request, Response]): TwitterFuture[Response] = {
    val timeoutFuture = ScalaFuture {
      Thread.sleep(timeout.inMillis)
      throw new TimeoutException(s"Timeout after ${timeout.inMillis} millis, probably problems with the connection with the database.")
    }

    val responseFuture = service(request).asScala
    ScalaFuture.firstCompletedOf(Seq(timeoutFuture, responseFuture)) asTwitter
  }
}

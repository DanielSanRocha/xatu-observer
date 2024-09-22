package com.danielsanrocha.xatu.filters

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import com.typesafe.scalalogging.Logger

class CORSFilter extends SimpleFilter[Request, Response] {
  val logging: Logger = Logger(this.getClass)

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request) map { response =>
      response.headerMap.add("Access-Control-Allow-Origin", "*")
      response.headerMap.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
      response.headerMap.add("Access-Control-Allow-Methods", "PUT, POST, GET, DELETE, OPTIONS");
      response
    }
  }
}

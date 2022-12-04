package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.filters.{TestExceptionHandlerFilter, TestSetRequestIdFilter}
import com.twitter.finatra.http.marshalling.DefaultMessageBodyWriter
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, EmbeddedHttpServer, HttpServer}

trait TestController {
  def createServer(controller: Controller): EmbeddedHttpServer = {
    val server = new HttpServer() {
      override def configureHttp(router: HttpRouter): Unit = router
        .filter(new TestExceptionHandlerFilter())
        .filter(new TestSetRequestIdFilter("test"))
        .add(controller)
        .register[DefaultMessageBodyWriter]
    }

    new EmbeddedHttpServer(server)
  }
}

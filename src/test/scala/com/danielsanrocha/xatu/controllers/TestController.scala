package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.filters.{ExceptionHandlerFilter, TestSetCredentialFilter, TestSetRequestIdFilter}
import com.danielsanrocha.xatu.models.internals.TimedCredential
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, EmbeddedHttpServer, HttpServer}

trait TestController {
  def createServer(controller: Controller): EmbeddedHttpServer = {
    val server = new HttpServer() {
      override def configureHttp(router: HttpRouter): Unit =
        router
          .filter(new TestSetRequestIdFilter("test"))
          .filter(new ExceptionHandlerFilter())
          .add(controller)
    }

    new EmbeddedHttpServer(server)
  }

  def createServer(controller: Controller, credential: TimedCredential): EmbeddedHttpServer = {
    val server = new HttpServer {
      override protected def configureHttp(router: HttpRouter): Unit = {
        router
          .filter(new TestSetRequestIdFilter("test"))
          .filter(new ExceptionHandlerFilter())
          .filter(new TestSetCredentialFilter(credential))
          .add(controller)
      }
    }
    new EmbeddedHttpServer(server)
  }
}

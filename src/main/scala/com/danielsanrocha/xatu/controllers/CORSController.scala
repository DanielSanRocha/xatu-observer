package com.danielsanrocha.xatu.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.typesafe.scalalogging.Logger

class CORSController extends Controller {
  val logging: Logger = Logger(this.getClass)

  options("/:*") { _: Request => response.ok("*") }
}

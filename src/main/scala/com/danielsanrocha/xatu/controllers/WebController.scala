package com.danielsanrocha.xatu.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class WebController(implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  get("/:*") { request: Request =>
    val path = request.params("*") match {
      case req if req.matches(raw".*\.[a-z]+") => "/dist/" + req
      case req                                 => "/dist/" + req + "/index.html"
    }

    val contentType = path match {
      case res if res.endsWith(".html") => "text/html"
      case res if res.endsWith(".js")   => "text/javascript"
      case res if res.endsWith(".css")  => "text/css"
      case res if res.endsWith(".ico")  => "image/x-icon"
      case res if res.endsWith(".png")  => "image/png"
      case res if res.endsWith(".jpg")  => "image/jpeg"
      case _                            => "application/octet-stream"
    }

    response.ok.fileOrIndex(path, "/dist/index.html").header("Content-Type", contentType)
  }
}

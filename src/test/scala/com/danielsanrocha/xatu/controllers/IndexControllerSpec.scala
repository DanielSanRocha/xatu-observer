package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.responses.ServerMessage
import com.twitter.finagle.http.Status

import scala.concurrent.Future

class IndexControllerSpec extends UnitSpec with TestController {
  describe("GET /api") {
    it("should return ok") {
      val controller = new IndexController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerMessage]("/api", andExpect = Status.Ok)
        response.message should equal("Welcome to Xatu Observer!")
        response.requestId should equal("test")
      }
    }
  }
}

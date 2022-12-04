package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.UnitSpec
import com.twitter.finagle.http.Status

import scala.concurrent.Future

class CORSControllerSpec extends UnitSpec with TestController {
  private val controller = new CORSController()
  private val server = createServer(controller)

  describe("Options CORSController") {
    it("should return ok to options anywhere") {
      val response = server.httpOptions("/path/to/anywhere", andExpect = Status.Ok)
      Future {
        response.status.code should equal(200)
      }
    }
  }
}

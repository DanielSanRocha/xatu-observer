package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.responses.ServerMessage
import com.twitter.finagle.http.Status

import scala.concurrent.Future

class NotFoundControllerSpec extends UnitSpec with TestController {
  private val controller = new NotFoundController()
  private val server = createServer(controller)

  describe("Path to anywhere") {
    it("should return 404 in GET to anywhere") {
      val result = server.httpGetJson[ServerMessage]("/api/path/to/anywhere", andExpect = Status.NotFound)
      Future {
        result.requestId should equal("test")
        result.message.toLowerCase should include("not found")
      }
    }

    it("should return 404 in POST to anywhere") {
      val result = server.httpPostJson[ServerMessage]("/api/somewhere/over/the/rainbow", postBody = "{\"message\": \"OK\"}", andExpect = Status.NotFound)
      Future {
        result.requestId should equal("test")
        result.message.toLowerCase should include("not found")
      }
    }

    it("should return 404 in PUT to anywhere") {
      val result = server.httpPutJson[ServerMessage]("/api/somewhere/over/the/rainbow", putBody = "{\"message\": \"OK\"}", andExpect = Status.NotFound)
      Future {
        result.requestId should equal("test")
        result.message.toLowerCase should include("not found")
      }
    }

    it("should return 404 in PATCH to anywhere") {
      val result = server.httpPatchJson[ServerMessage]("/api/somewhere/over/the/rainbow", patchBody = "{\"message\": \"OK\"}", andExpect = Status.NotFound)
      Future {
        result.requestId should equal("test")
        result.message.toLowerCase should include("not found")
      }
    }

    it("should return 404 in DELETE to anywhere") {
      val result = server.httpDeleteJson[ServerMessage]("/api/somewhere/over/the/rainbow", deleteBody = "", andExpect = Status.NotFound)
      Future {
        result.requestId should equal("test")
        result.message.toLowerCase should include("not found")
      }
    }
  }
}

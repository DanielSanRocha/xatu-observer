package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.internals.TimedCredential
import com.danielsanrocha.xatu.models.responses.{ServerMessage, UserResponse}
import com.danielsanrocha.xatu.repositories.TestRepository
import com.danielsanrocha.xatu.services.UserService
import com.twitter.finagle.http.Status
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, times, verify, when}

import java.sql.Timestamp
import scala.concurrent.Future

class UserControllerSpec extends UnitSpec with TestController with TestRepository {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  describe("GET /user") {
    it("should return the user") {
      implicit val service: UserService = mock(classOf[UserService])
      val controller = new UserController()

      val user = UserResponse(10, "jujuba", "jujuba@mail.com", new Timestamp(0), new Timestamp(0))
      when(service.getById(10)).thenReturn(Future(Some(user)))

      val credential = TimedCredential(10, "jujuba@mail.com", System.currentTimeMillis())
      val server = createServer(controller, credential)

      Future {
        val result = server.httpGetJson[UserResponse]("/user", andExpect = Status.Ok)
        verify(service, times(1)).getById(any)
        verify(service, times(1)).getById(10)
        result should equal(user)
      }
    }

    it("should responds with InternalServerError if user not found") {
      implicit val service: UserService = mock(classOf[UserService])
      val controller = new UserController()

      when(service.getById(10)).thenReturn(Future(None))

      val credential = TimedCredential(10, "jujuba@mail.com", System.currentTimeMillis())
      val server = createServer(controller, credential)

      Future {
        val result = server.httpGetJson[ServerMessage]("/user", andExpect = Status.InternalServerError)
        verify(service, times(1)).getById(any)
        verify(service, times(1)).getById(10)
        result.message should include("strange")
        result.requestId should equal("test")
      }
    }
  }
}

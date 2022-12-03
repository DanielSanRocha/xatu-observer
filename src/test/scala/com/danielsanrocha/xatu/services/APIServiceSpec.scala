package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.internals.API
import com.danielsanrocha.xatu.repositories.APIRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, times, verify, when}

import java.sql.Timestamp
import scala.concurrent.Future

class APIServiceSpec extends UnitSpec {
  describe("method getById") {
    it("should return API if found") {
      implicit val repository: APIRepository = mock(classOf[APIRepository])

      val api: API = API(id = 7, name = "Jujuba", host = "http://localhost", port = 8000, healthcheckRoute = "/healthcheck", status = 'W', createDate = new Timestamp(0), updateDate = new Timestamp(0))

      when(repository.getById(7)).thenReturn(Future(Some(api)))

      val service = new APIServiceImpl()

      service.getById(7) map {
        case Some(response) =>
          verify(repository, times(1)).getById(any)
          verify(repository, times(1)).getById(7)
          verify(repository, times(0)).getAll(any, any)

          response should equal(api)

        case None => fail("should returned a User but returned None")
      }
    }

    it("should return None if user not found") {
      implicit val repository = mock(classOf[APIRepository])

      val api: API = API(id = 7, name = "Jujuba", host = "http://localhost", port = 8000, healthcheckRoute = "/healthcheck", status = 'W', createDate = new Timestamp(0), updateDate = new Timestamp(0))
      when(repository.getById(7)).thenReturn(Future(Some(api)))
      when(repository.getById(8)).thenReturn(Future(None))

      val service = new APIServiceImpl()

      service.getById(8) map {
        case Some(_) => fail("should have returned None but returned an User")
        case None =>
          verify(repository, times(1)).getById(any)
          verify(repository, times(1)).getById(8)
          succeed
      }
    }
  }
}

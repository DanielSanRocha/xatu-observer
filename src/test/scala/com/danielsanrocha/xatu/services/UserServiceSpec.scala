package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.internals.User
import com.danielsanrocha.xatu.models.responses.UserResponse
import com.danielsanrocha.xatu.repositories.UserRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, times, verify, when}

import java.sql.Timestamp
import scala.concurrent.Future

class UserServiceSpec extends UnitSpec {
  describe("method getById") {
    it("should return User if found") {
      implicit val repository = mock(classOf[UserRepository])

      val user: User = User(id = 7, name = "Jujuba", email = "jujuba@mail.com", password = "1234", createDate = new Timestamp(0), updateDate = new Timestamp(0))
      val userResponse: UserResponse = UserResponse(id = 7, name = "Jujuba", email = "jujuba@mail.com", createDate = new Timestamp(0), updateDate = new Timestamp(0))

      when(repository.getById(7)).thenReturn(Future(Some(user)))

      val service = new UserServiceImpl()

      service.getById(7) map {
        case Some(response) =>
          verify(repository, times(1)).getById(any)
          verify(repository, times(1)).getById(7)
          verify(repository, times(0)).getByEmail(any)

          response should equal(userResponse)

        case None => fail("should returned a User but returned None")
      }
    }

    it("should return None if user not found") {
      implicit val repository = mock(classOf[UserRepository])

      val user: User = User(id = 7, name = "Jujuba", email = "jujuba@mail.com", password = "1234", createDate = new Timestamp(0), updateDate = new Timestamp(0))
      when(repository.getById(7)).thenReturn(Future(Some(user)))
      when(repository.getById(8)).thenReturn(Future(None))

      val service = new UserServiceImpl()

      service.getById(8) map {
        case Some(_) => fail("should have returned None but returned an User")
        case None => {
          verify(repository, times(1)).getById(any)
          verify(repository, times(1)).getById(8)

          succeed
        }
      }
    }
  }
}

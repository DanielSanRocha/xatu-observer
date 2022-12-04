package com.danielsanrocha.xatu.repositories

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.commons.Security

class UserRepositorySpec extends UnitSpec with TestRepository {
  runQuery(s"INSERT INTO tb_users (id, name, email, password) VALUES (7, 'Jujuba', 'jujuba@mail.com', '${Security.hash("1234")}');")

  describe("method getByEmail") {
    it("should return User if found") {
      val repository = new UserRepositoryImpl()

      repository.getByEmail("jujuba@mail.com") map {
        case Some(user) =>
          user.email should equal("jujuba@mail.com")
          user.name should equal("Jujuba")
        case None =>
          fail("Should return user, returned None")
      }
    }

    it("should return None if not found") {
      val repository = new UserRepositoryImpl()

      repository.getByEmail("jujuba2@mail.com") map {
        case Some(_) => fail("Should return None, returned a user")
        case None    => succeed
      }
    }
  }

  describe("method getById") {
    it("should return User if found") {
      val repository = new UserRepositoryImpl()

      repository.getById(7L) map {
        case Some(user) =>
          user.email should equal("jujuba@mail.com")
          user.name should equal("Jujuba")
        case None =>
          fail("Should return user, returned None")
      }
    }

    it("should return None if not found") {
      val repository = new UserRepositoryImpl()

      repository.getById(8) map {
        case Some(_) => fail("Should return None, returned a user")
        case None    => succeed
      }
    }
  }
}

package com.danielsanrocha.xatu.repositories

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.internals.NewContainer
import slick.jdbc.MySQLProfile.api._

import scala.language.postfixOps

class ContainerRepositorySpec extends UnitSpec with TestRepository {
  runQuery(s"INSERT INTO tb_containers (id, name) VALUES (7, 'Jujuba')")
  runQuery(s"INSERT INTO tb_containers (id, name) VALUES (13, 'Amoeba')")

  describe("getById method") {
    it("should return Container if it exists") {
      val repository = new ContainerRepositoryImpl()

      repository.getById(13) map {
        case Some(container) =>
          container.id should equal(13)
          container.name should equal("Amoeba")
        case None => fail("returned None but it should return a Container")
      }
    }

    it("should return None if it not exists") {
      val repository = new ContainerRepositoryImpl()

      repository.getById(17) map {
        case Some(_) => fail("returned a Container when it should return None")
        case None    => succeed
      }
    }
  }

  describe("getAll method") {
    it("should return all database with limit 100 and offset 0") {
      val repository = new ContainerRepositoryImpl()

      repository.getAll(100, 0) map { containers =>
        containers.map(_.name).toSet should equal(Set("Jujuba", "Amoeba"))
        containers.map(_.id).toSet should equal(Set(7, 13))
      }
    }

    it("should return just one entry when limit 100 and offset 1") {
      val repository = new ContainerRepositoryImpl()

      repository.getAll(100, 1) map { containers =>
        containers.map(_.name).toSet should equal(Set("Amoeba"))
        containers.map(_.id).toSet should equal(Set(13))
      }
    }

    it("should return the first Container in the database with limit 1 and offset 0") {
      val repository = new ContainerRepositoryImpl()

      repository.getAll(1, 0) map { containers =>
        containers.map(_.name) should equal(Seq("Jujuba"))
        containers.map(_.id) should equal(Seq(7))
      }
    }
  }

  describe("create method") {
    it("should create a Container") {
      val repository = new ContainerRepositoryImpl()

      repository.create(NewContainer("aleluia")) map { id =>
        client.run(sql"SELECT id,name FROM tb_containers WHERE id=$id".as[(Long, String)]) map { tuple =>
          tuple.map(_._2) should equal(Seq("aleluia"))
          tuple.map(_._1) should equal(Seq(id))

          runQuery(s"DELETE FROM tb_containers WHERE id=$id")
          succeed
        }
      } flatten
    }
  }

  describe("delete method") {
    it("should delete a Container and return true when succeeded") {
      runQuery("INSERT INTO tb_containers (id, name) VALUES (17, 'aleluia')")
      val repository = new ContainerRepositoryImpl()

      repository.delete(17) map { b =>
        client.run(sql"SELECT id,name FROM tb_containers where id=17".as[(Long, String)]).map { containers =>
          containers should equal(Seq())
          b should equal(true)
        }
      } flatten
    }

    it("should not delete a Container and return false if the container does not exists") {
      val repository = new ContainerRepositoryImpl()

      repository.delete(24) map { b =>
        b should equal(false)
      }
    }
  }
}

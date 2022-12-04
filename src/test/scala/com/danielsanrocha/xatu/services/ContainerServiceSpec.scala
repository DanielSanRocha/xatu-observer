package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.internals.{Container, ContainerInfo, NewContainer}
import com.danielsanrocha.xatu.repositories.ContainerRepository
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.ListContainersCmd
import com.github.dockerjava.api.model.{Container => DockerContainer}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, times, verify, when}

import java.sql.Timestamp
import scala.concurrent.Future

class ContainerServiceSpec extends UnitSpec {
  describe("getById method") {
    it("should return a Container if it exists") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      val container = Container(7, "jujuba", new Timestamp(0), new Timestamp(0))
      when(repository.getById(7)).thenReturn(Future(Some(container)))

      val service = new ContainerServiceImpl()
      service.getById(7) map {
        case Some(cont) =>
          verify(repository, times(1)).getById(any)
          verify(repository, times(1)).getById(7)
          cont should equal(container)

        case None => fail("Should return a Container, returned None")
      }
    }

    it("should return None if the id is not found") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      when(repository.getById(12)).thenReturn(Future(None))

      val service = new ContainerServiceImpl()
      service.getById(12) map {
        case Some(_) => fail("should return None, returned a Container")
        case None =>
          verify(repository, times(1)).getById(any)
          verify(repository, times(1)).getById(12)
          succeed
      }
    }
  }

  describe("create method") {
    it("should create a new Container") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      val cont = NewContainer("jujuba")
      when(repository.create(any)).thenReturn(Future(13L))

      repository.create(cont) map { result =>
        result should equal(13L)
        verify(repository, times(1)).create(any)
        verify(repository, times(1)).create(cont)
        succeed
      }
    }
  }

  describe("delete method") {
    it("should delete a Container and return true if succeeded") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      val cont = NewContainer("jujuba")
      when(repository.delete(14)).thenReturn(Future(true))

      repository.delete(14) map { result =>
        result should equal(true)
        verify(repository, times(1)).delete(any)
        verify(repository, times(1)).delete(14)
        succeed
      }
    }

    it("should delete a Container and return false if fail") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      val cont = NewContainer("jujuba")
      when(repository.delete(14)).thenReturn(Future(false))

      repository.delete(14) map { result =>
        result should equal(false)
        verify(repository, times(1)).delete(any)
        verify(repository, times(1)).delete(14)
        succeed
      }
    }
  }

  describe("getAll method") {
    it("should get all containers inactive limit 10 offset 7") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      val container1 = Container(5, "Jujuba", new Timestamp(0), new Timestamp(0))
      val container2 = Container(7, "Aleluia", new Timestamp(0), new Timestamp(0))
      when(repository.getAll(any, any)).thenReturn(Future(Seq(container1, container2)))

      val cmd: ListContainersCmd = mock(classOf[ListContainersCmd])
      when(cmd.exec()).thenReturn(new java.util.ArrayList[DockerContainer]())
      when(dockerClient.listContainersCmd()).thenReturn(cmd)

      val service = new ContainerServiceImpl()

      service.getAll(10, 7) map { containers =>
        containers.length should equal(2)
        containers.map(_.id) should equal(Seq(5, 7))
        containers.map(_.name) should equal(Seq("Jujuba", "Aleluia"))

        verify(repository, times(1)).getAll(10, 7)
        verify(repository, times(1)).getAll(any, any)
        containers.map(_.info) should equal(Seq(None, None))
        containers.map(_.status) should equal(Seq('F', 'F'))
      }
    }
  }

  describe("getAll method") {
    it("should get all containers inactive limit 3 offset 2") {
      implicit val repository: ContainerRepository = mock(classOf[ContainerRepository])
      implicit val dockerClient: DockerClient = mock(classOf[DockerClient])

      val container1 = Container(5, "Jujuba", new Timestamp(0), new Timestamp(0))
      val container2 = Container(7, "Aleluia", new Timestamp(0), new Timestamp(0))
      when(repository.getAll(any, any)).thenReturn(Future(Seq(container1, container2)))

      val cmd: ListContainersCmd = mock(classOf[ListContainersCmd])
      val docker1 = mock(classOf[DockerContainer])
      val docker2 = mock(classOf[DockerContainer])
      val dockers = new java.util.ArrayList[DockerContainer]()
      dockers.add(docker1)
      dockers.add(docker2)
      when(cmd.exec()).thenReturn(dockers)
      when(docker1.getNames).thenReturn(Array[String]("/Aleluia"))
      when(docker2.getNames).thenReturn(Array[String]("/Amoeba"))
      when(docker1.getImage).thenReturn("Image1")
      when(docker2.getImage).thenReturn("Image2")
      when(docker1.getId).thenReturn("1234")
      when(docker2.getId).thenReturn("4321")
      when(dockerClient.listContainersCmd()).thenReturn(cmd)

      val service = new ContainerServiceImpl()

      service.getAll(3, 2) map { containers =>
        containers.length should equal(2)
        containers.map(_.id) should equal(Seq(5, 7))
        containers.map(_.name) should equal(Seq("Jujuba", "Aleluia"))

        verify(repository, times(1)).getAll(3, 2)
        verify(repository, times(1)).getAll(any, any)
        containers.map(_.info) should equal(Seq(None, Some(ContainerInfo("Image1", "1234"))))
        containers.map(_.status) should equal(Seq('F', 'W'))
      }
    }
  }
}

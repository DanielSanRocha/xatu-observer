package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.UnitSpec
import com.danielsanrocha.xatu.models.responses.ServerStatus
import com.danielsanrocha.xatu.repositories.{LogRepository, TestRepository}
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PingCmd
import com.twitter.finagle.http.Status
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import redis.clients.jedis.{Jedis, JedisPool}
import redis.clients.jedis.exceptions.{JedisException, JedisConnectionException}

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class HealthcheckControllerSpec extends UnitSpec with TestController with TestRepository {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val timeout: FiniteDuration = new FiniteDuration(1, TimeUnit.SECONDS)

  describe("GET /healthcheck") {
    it("should return ok if all repositories are ok") {
      val cache: Jedis = mock[Jedis]
      implicit val cachePool: JedisPool = mock[JedisPool]
      implicit val dockerClient: DockerClient = mock[DockerClient]
      implicit val logRepository: LogRepository = mock[LogRepository]

      val randomCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(cachePool.getResource).thenReturn(cache)
      when(cache.set(anyString(), randomCapture.capture)).thenReturn("OK")
      when(cache.get("random")).thenAnswer(_ => randomCapture.getValue)

      when(logRepository.status()).thenReturn(Future())

      val pingCmd = mock[PingCmd]
      when(dockerClient.pingCmd()).thenReturn(pingCmd)

      val controller = new HealthcheckController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerStatus]("/healthcheck", andExpect = Status.Ok)
        verify(cache, times(1)).close()
        verify(cache, times(1)).close()
        response.redis should equal("Ok")
        response.mysql should equal("Ok")
        response.docker should equal("Ok")
        response.elasticsearch should equal("Ok")
      }
    }

    it("should return 500 if redis is misbehaving") {
      val cache: Jedis = mock[Jedis]
      implicit val cachePool: JedisPool = mock[JedisPool]
      implicit val dockerClient: DockerClient = mock[DockerClient]
      implicit val logRepository: LogRepository = mock[LogRepository]

      when(cache.set(anyString(), anyString())).thenReturn("OK")
      when(cache.get("random")).thenAnswer(_ => "1234")
      when(cachePool.getResource).thenReturn(cache)

      when(logRepository.status()).thenReturn(Future())

      val pingCmd = mock[PingCmd]
      when(dockerClient.pingCmd()).thenReturn(pingCmd)

      val controller = new HealthcheckController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerStatus]("/healthcheck", andExpect = Status.InternalServerError)
        verify(cache, times(1)).close()
        response.redis should not equal ("Ok")
        response.mysql should equal("Ok")
        response.docker should equal("Ok")
        response.elasticsearch should equal("Ok")
      }
    }

    it("should return 500 if redis throws an exception") {
      implicit val cachePool: JedisPool = mock[JedisPool]
      implicit val cache: Jedis = mock[Jedis]
      implicit val dockerClient: DockerClient = mock[DockerClient]
      implicit val logRepository: LogRepository = mock[LogRepository]

      when(cache.set(anyString(), anyString())).thenReturn("OK")
      when(cache.get("random")).thenThrow(new JedisException("Not working..."))
      when(cachePool.getResource).thenReturn(cache)

      when(logRepository.status()).thenReturn(Future())

      val pingCmd = mock[PingCmd]
      when(dockerClient.pingCmd()).thenReturn(pingCmd)

      val controller = new HealthcheckController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerStatus]("/healthcheck", andExpect = Status.InternalServerError)
        verify(cache, times(1)).close()
        verify(cache, times(1)).close()
        response.redis should equal("Not working...")
        response.mysql should equal("Ok")
        response.docker should equal("Ok")
        response.elasticsearch should equal("Ok")
      }
    }

    it("should return 500 if could not connect to redis") {
      implicit val cachePool: JedisPool = mock[JedisPool]
      implicit val dockerClient: DockerClient = mock[DockerClient]
      implicit val logRepository: LogRepository = mock[LogRepository]

      when(cachePool.getResource).thenThrow(new JedisConnectionException("jujuba"))

      when(logRepository.status()).thenReturn(Future())

      val pingCmd = mock[PingCmd]
      when(dockerClient.pingCmd()).thenReturn(pingCmd)

      val controller = new HealthcheckController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerStatus]("/healthcheck", andExpect = Status.InternalServerError)
        response.redis should equal("jujuba")
        response.mysql should equal("Ok")
        response.docker should equal("Ok")
        response.elasticsearch should equal("Ok")
      }
    }

    it("should return 500 if LogRepository throws an exception") {
      implicit val cache: Jedis = mock[Jedis]
      implicit val cachePool: JedisPool = mock[JedisPool]
      implicit val dockerClient: DockerClient = mock[DockerClient]
      implicit val logRepository: LogRepository = mock[LogRepository]

      val randomCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(cachePool.getResource).thenReturn(cache)
      when(cache.set(anyString(), randomCapture.capture)).thenReturn("OK")
      when(cache.get("random")).thenAnswer(_ => randomCapture.getValue)

      when(logRepository.status()).thenReturn(Future {
        throw new Exception("jujuba")
      })

      val pingCmd = mock[PingCmd]
      when(dockerClient.pingCmd()).thenReturn(pingCmd)

      val controller = new HealthcheckController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerStatus]("/healthcheck", andExpect = Status.InternalServerError)
        verify(cache, times(1)).close()
        verify(cache, times(1)).close()
        response.redis should equal("Ok")
        response.mysql should equal("Ok")
        response.docker should equal("Ok")
        response.elasticsearch should equal("jujuba")
      }
    }

    it("should return 500 if DockerClient throws an exception") {
      implicit val cache: Jedis = mock[Jedis]
      implicit val cachePool: JedisPool = mock[JedisPool]
      implicit val dockerClient: DockerClient = mock[DockerClient]
      implicit val logRepository: LogRepository = mock[LogRepository]

      val randomCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(cachePool.getResource).thenReturn(cache)
      when(cache.set(anyString(), randomCapture.capture)).thenReturn("OK")
      when(cache.get("random")).thenAnswer(_ => randomCapture.getValue)

      when(logRepository.status()).thenReturn(Future())

      val pingCmd = mock[PingCmd]
      when(pingCmd.exec()).thenThrow(new NullPointerException("aleluia"))
      when(dockerClient.pingCmd()).thenReturn(pingCmd)

      val controller = new HealthcheckController()
      val server = createServer(controller)

      Future {
        val response = server.httpGetJson[ServerStatus]("/healthcheck", andExpect = Status.InternalServerError)
        verify(cache, times(1)).close()
        verify(cache, times(1)).close()
        response.redis should equal("Ok")
        response.mysql should equal("Ok")
        response.docker should equal("aleluia")
        response.elasticsearch should equal("Ok")
      }
    }
  }
}

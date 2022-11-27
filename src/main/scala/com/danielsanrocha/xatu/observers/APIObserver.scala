package com.danielsanrocha.xatu.observers

import java.util.concurrent._
import com.twitter.util.logging.Logger
import scalaj.http.{Http, HttpOptions}
import com.danielsanrocha.xatu.models.internals.API
import com.danielsanrocha.xatu.services.APIService

class APIObserver(api: API, implicit val service: APIService) {
  private val logging: Logger = Logger(this.getClass)

  private val ex = new ScheduledThreadPoolExecutor(1)

  private var _api = api

  def setAPI(api: API): Unit = { _api = api }
  def getAPI(): API = _api

  private val task = new Runnable {
    def run(): Unit = {
      val route = s"${_api.host}:${_api.port}${_api.healthcheckRoute}"
      logging.info(s"Making request to API(id, name) = (${api.id}, ${api.name}) route $route")
      try {
        val result = Http(route).option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(10000)).execute()
        result.code match {
          case 200 => {
            logging.debug(s"Healthcheck route at $route returned 200, setting status working...")
            service.setStatus(_api.id, 'W')
          }
          case _ => {
            logging.debug(s"Healthcheck route at $route returned not 200, setting status fail...")
            service.setStatus(_api.id, 'F')
          }
        }
      } catch {
        case e: Exception => {
          logging.warn(s"Network problem with API(id = ${_api.id}, name = ${_api.name}) route $route. Exception: ${e.getMessage}")
          logging.debug(s"Setting status to failed API(id = ${_api.id}, name = ${_api.name})")
          service.setStatus(_api.id, 'F')
        }
      }
    }
  }

  def stop(): Unit = {
    interval.cancel(true)
  }

  logging.info(s"Starting Observer for API(id, name) = (${api.id}, ${api.name})")
  private val interval = ex.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS)
}

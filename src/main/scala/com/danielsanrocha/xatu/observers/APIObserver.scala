package com.danielsanrocha.xatu.observers

import com.danielsanrocha.xatu.models.internals.API
import com.danielsanrocha.xatu.services.APIService
import com.typesafe.scalalogging.Logger
import scalaj.http.{Http, HttpOptions}

class APIObserver(api: API, implicit val service: APIService) extends Observer[API](api) {
  private val logging: Logger = Logger(this.getClass)

  override lazy val task: Runnable = () => {
    val route = s"${_data.host}:${_data.port}${_data.healthcheckRoute}"
    logging.info(s"Making request to API(id, name) = (${api.id}, ${api.name}) route $route")
    try {
      val result = Http(route).option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(10000)).execute()
      result.code match {
        case 200 =>
          logging.debug(s"Healthcheck route at $route returned 200, setting status working...")
          service.setStatus(_data.id, 'W')
        case _ =>
          logging.debug(s"Healthcheck route at $route returned not 200, setting status fail...")
          service.setStatus(_data.id, 'F')
      }
    } catch {
      case e: Exception =>
        logging.error(s"Network problem with API(id = ${_data.id}, name = ${_data.name}) route $route. Exception: ${e.getMessage}")
        logging.debug(s"Setting status to failed API(id = ${_data.id}, name = ${_data.name})")
        service.setStatus(_data.id, 'F')
    }
  }
}

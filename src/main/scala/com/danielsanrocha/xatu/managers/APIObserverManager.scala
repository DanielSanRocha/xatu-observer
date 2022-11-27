package com.danielsanrocha.xatu.managers

import java.util.concurrent._
import scala.collection.mutable.Map
import com.twitter.util.logging.Logger

import com.danielsanrocha.xatu.observers.APIObserver
import com.danielsanrocha.xatu.services.APIService

class APIObserverManager(implicit val service: APIService, implicit val ec: scala.concurrent.ExecutionContext) {
  private val logging: Logger = Logger(this.getClass)

  private val observers: Map[Long, APIObserver] = Map()

  private val ex = new ScheduledThreadPoolExecutor(1)

  private val task = new Runnable {
    def run() = {
      service.getAll(1000, 0) map { apis =>
        apis.foreach(api => {
          observers.get(api.id) match {
            case Some(obs) =>
              logging.debug(s"Updating parameters for API(${api.id}, ${api.name})")
              obs.setAPI(api)

            case None =>
              logging.info(s"Creating observer for api with id ${api.id} and name ${api.name}")
              val observer = new APIObserver(api, service)
              observers.addOne((api.id, observer))

          }
        })

        val idSet = apis.map(_.id)
        logging.debug(s"Ids on database ${idSet.mkString(",")}. Killing observers of deleted apis...")

        observers.foreach(tuple => {
          val id = tuple._1
          val obs = tuple._2

          idSet.find(_ == id) match {
            case None =>
              logging.info(s"Killing observer for api with id ${obs.getAPI().id} and name ${obs.getAPI().name}")
              obs.stop()
              observers.remove(id)
          }
        })
      }
    }
  }

  private val interval = ex.scheduleAtFixedRate(task, 10, 10, TimeUnit.SECONDS)
}

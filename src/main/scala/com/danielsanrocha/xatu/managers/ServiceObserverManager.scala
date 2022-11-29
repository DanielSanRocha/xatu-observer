package com.danielsanrocha.xatu.managers

import java.util.concurrent._
import scala.collection.mutable.Map
import com.twitter.util.logging.Logger

import com.danielsanrocha.xatu.observers.ServiceObserver
import com.danielsanrocha.xatu.services.ServiceService

class ServiceObserverManager(implicit val service: ServiceService, implicit val ec: scala.concurrent.ExecutionContext) {
  private val logging: Logger = Logger(this.getClass)

  private val observers: Map[Long, ServiceObserver] = Map()

  private val ex = new ScheduledThreadPoolExecutor(1)

  private val task = new Runnable {
    def run() = {
      service.getAll(1000, 0) map { services =>
        services.foreach(s => {
          observers.get(s.id) match {
            case Some(obs) =>
              logging.debug(s"Updating parameters for Service(${s.id}, ${s.name})")
              obs.setService(s)

            case None =>
              logging.info(s"Creating observer for service with id ${s.id} and name ${s.name}")
              val observer = new ServiceObserver(s, service)
              observers.addOne((s.id, observer))

          }
        })

        val idSet = services.map(_.id)
        logging.debug(s"Ids on database ${idSet.mkString(",")}. Killing observers of deleted services...")

        observers.foreach(tuple => {
          val id = tuple._1
          val obs = tuple._2

          idSet.find(_ == id) match {
            case None =>
              logging.info(s"Killing observer for service with id ${obs.getService().id} and name ${obs.getService().name}")
              obs.stop()
              observers.remove(id)
          }
        })
      }
    }
  }

  private val interval = ex.scheduleAtFixedRate(task, 10, 10, TimeUnit.SECONDS)
}

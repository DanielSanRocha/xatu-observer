package com.danielsanrocha.xatu.managers

import com.twitter.util.logging.Logger

import scala.collection.mutable.Map
import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import com.danielsanrocha.xatu.services.Service
import com.danielsanrocha.xatu.models.internals.Data
import com.danielsanrocha.xatu.observers.Observer

import scala.collection.mutable

abstract class Manager[DATA <: Data, OBSERVER <: Observer[DATA]](service: Service[DATA], implicit val ec: scala.concurrent.ExecutionContext) {
  private val logging: Logger = Logger(this.getClass)

  private val observers: mutable.Map[Long, OBSERVER] = mutable.Map()

  private val ex = new ScheduledThreadPoolExecutor(1)

  protected def createObserver(data: DATA): OBSERVER

  def status(): mutable.Map[Long, String] = {
    observers map { tuple =>
      tuple._1 -> tuple._2.status
    }
  }

  protected val task: Runnable = () => {
    service.getAll(1000, 0) map { objects =>
      objects.foreach { obj =>
        observers.get(obj.id) match {
          case Some(obs) =>
            logging.debug(s"Observer for '${obj.name}' already created")
            logging.debug(s"Checking for parameter changes Container(${obj.id},${obj.name})")
            if (obj != obs.getData) {
              logging.debug(s"Updating parameters for (${obj.id}, ${obj.name})")
              obs.reload(obj)
            }
          case None =>
            logging.info(s"Creating log observer for container with id ${obj.id} and name ${obj.name}")
            val observer = createObserver(obj)
            observers.addOne((obj.id, observer))
        }
      }

      val idSet = objects.map(_.id)
      logging.debug(s"Ids on database ${idSet.mkString(",")}. Killing observers of deleted/stopped objects...")

      observers.foreach(tuple => {
        val id = tuple._1
        val obs = tuple._2

        idSet.find(_ == id) match {
          case None =>
            logging.info(s"Killing log observer for container with id $id...")
            obs.stop()
            observers.remove(id)
          case Some(id) =>
            logging.info(s"Keeping observer for id ${id}")
        }
      })
    }
  }

  protected val interval: ScheduledFuture[_] = ex.scheduleAtFixedRate(task, 10, 10, TimeUnit.SECONDS)
}
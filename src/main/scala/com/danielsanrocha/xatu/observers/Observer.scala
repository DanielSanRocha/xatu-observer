package com.danielsanrocha.xatu.observers

import com.danielsanrocha.xatu.models.internals.{Data, SimpleStatus, Status}
import com.twitter.util.logging.Logger

import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}

abstract class Observer[DATA <: Data](d: DATA) {
  private val logging: Logger = Logger(this.getClass)

  protected var _data: DATA = d

  def getData: DATA = _data

  def reload(data: DATA): Unit = {
    _data = data
  }

  def status(): Status = {
    SimpleStatus(_data.id, _data.name)
  }

  def stop(): Unit = {
    interval.cancel(true)
  }

  val task: Runnable

  private val ex = new ScheduledThreadPoolExecutor(1)

  var interval: ScheduledFuture[_] = null

  def start(): Unit = {
    logging.info(s"Starting Observer for Data with id ${_data.id} and name ${_data.name}")
    interval = ex.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS)
  }
}

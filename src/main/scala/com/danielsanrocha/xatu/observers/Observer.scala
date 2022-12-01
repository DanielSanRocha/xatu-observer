package com.danielsanrocha.xatu.observers

import com.danielsanrocha.xatu.models.internals.Data
import com.twitter.util.logging.Logger

import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

abstract class Observer[DATA <: Data](d: DATA) {
  private val logging: Logger = Logger(this.getClass)

  protected var _data: DATA = d

  def getData: DATA = _data

  def reload(data: DATA): Unit = {
    _data = data
  }

  def status(): String = {
    _data.toString
  }

  def stop(): Unit = {
    interval.cancel(true)
  }

  protected val task: Runnable

  private val ex = new ScheduledThreadPoolExecutor(1)
  logging.info(s"Starting Observer for Data with id ${_data.id}, and name ${_data.name}")
  protected val interval = ex.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS)
}

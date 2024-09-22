package com.danielsanrocha.xatu.commons

import com.danielsanrocha.xatu.exceptions.TimeoutException

import java.util.{Timer, TimerTask}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}

object FutureTimeout {
  private val timer: Timer = new Timer(true)

  implicit class RichTimeoutFuture[A](val f: Future[A]) extends AnyVal {
    def withTimeout(implicit ec: ExecutionContext, timeout: FiniteDuration): Future[A] = {
      val promise = Promise[A]()

      val timerTask = new TimerTask() {
        def run(): Unit = {
          promise.tryFailure(new TimeoutException("timeout"))
        }
      }

      timer.schedule(timerTask, timeout.toMillis)

      f.map { a =>
        if (promise.trySuccess(a)) {
          timerTask.cancel()
        }
      }.recover { case e: Exception =>
        if (promise.tryFailure(e)) {
          timerTask.cancel()
        }
      }

      promise.future
    }
  }
}

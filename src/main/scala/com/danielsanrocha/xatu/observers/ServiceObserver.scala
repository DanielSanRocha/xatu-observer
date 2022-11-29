package com.danielsanrocha.xatu.observers

import java.util.concurrent._
import java.io.{BufferedReader, InputStreamReader}
import com.twitter.util.logging.Logger
import scalaj.http.{Http, HttpOptions}

import com.danielsanrocha.xatu.models.internals.Service
import com.danielsanrocha.xatu.services.ServiceService

class ServiceObserver(s: Service, implicit val service: ServiceService) {
  private val logging: Logger = Logger(this.getClass)

  private val ex = new ScheduledThreadPoolExecutor(1)

  private var _s = s

  def setService(s: Service): Unit = { _s = s }
  def getService(): Service = _s

  private val task = new Runnable {
    def run(): Unit = {
      try {
        logging.debug(s"Checking service ${_s.name}...")
        val command = scala.collection.JavaConverters.seqAsJavaList(Seq("/usr/bin/systemctl", "is-active", _s.name))
        val process = new ProcessBuilder(command).redirectErrorStream(true).start()
        process.waitFor()
        val in = process.getInputStream()
        val br = new BufferedReader(new InputStreamReader(in))
        val line = br.readLine()
        logging.debug(s"Status for service ${_s.name}: " + line)

        line match {
          case "active" => {
            logging.debug(s"Service ${_s.name} active, setting status to W...")
            service.setStatus(_s.id, 'W')
          }
          case _ => {
            logging.debug(s"Service ${_s.name} not active, setting status to F...")
            service.setStatus(_s.id, 'F')
          }
        }
      } catch {
        case e: Exception =>
          logging.error(s"Error retrieving service ${_s.name} status. Setting status to F...")
          service.setStatus(_s.id, 'F')
      }
    }
  }

  def stop(): Unit = {
    interval.cancel(true)
  }

  logging.info(s"Starting Observer for Service(id, name) = (${s.id}, ${s.name})")
  private val interval = ex.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS)
}

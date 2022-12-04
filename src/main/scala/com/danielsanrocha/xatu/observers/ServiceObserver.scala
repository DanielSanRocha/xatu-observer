package com.danielsanrocha.xatu.observers

import com.danielsanrocha.xatu.models.internals.Service
import com.danielsanrocha.xatu.services.ServiceService
import com.twitter.util.logging.Logger

import java.io.{BufferedReader, InputStreamReader}

class ServiceObserver(s: Service, implicit val service: ServiceService) extends Observer[Service](s) {
  private val logging: Logger = Logger(this.getClass)

  override lazy val task: Runnable = () => {
    try {
      logging.debug(s"Checking service ${_data.name}...")
      val command = scala.collection.JavaConverters.seqAsJavaList(Seq("systemctl", "is-active", _data.name))
      val process = new ProcessBuilder(command).redirectErrorStream(true).start()
      process.waitFor()
      val in = process.getInputStream
      val br = new BufferedReader(new InputStreamReader(in))
      val line = br.readLine()
      logging.debug(s"Status for service ${_data.name}: " + line)

      line match {
        case "active" => {
          logging.debug(s"Service ${_data.name} active, setting status to W...")
          service.setStatus(_data.id, 'W')
        }
        case _ => {
          logging.debug(s"Service ${_data.name} not active, setting status to F...")
          service.setStatus(_data.id, 'F')
        }
      }
    } catch {
      case e: Exception =>
        logging.error(s"Error retrieving service ${_data.name} status. Message:${e.getMessage}. Setting status to F...")
        service.setStatus(_data.id, 'F')
    }
  }
}

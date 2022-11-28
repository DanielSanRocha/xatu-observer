package com.danielsanrocha.xatu.observers

import com.twitter.util.logging.Logger

import java.nio.file.Paths
import scala.collection.mutable.Map
import java.io.{BufferedReader, File, FileReader}
import java.util.concurrent._

import com.danielsanrocha.xatu.models.internals.{Service, LogService => LogServiceModel}
import com.danielsanrocha.xatu.services.{LogService, ServiceService}

class LogServiceObserver(s: Service, implicit val service: ServiceService, implicit val logService: LogService) {
  private val logging: Logger = Logger(this.getClass)

  private val ex = new ScheduledThreadPoolExecutor(1)

  private var _service: Service = s
  private val files: Map[String, BufferedReader] = Map()

  def getService(): Service = _service

  private val task = new Runnable {
    def run(): Unit = {
      logging.debug(s"Searching for files Service(${_service.id}, ${_service.name})...")

      val regex = raw"${_service.logFileRegex}".r
      val directoryPath = new File(_service.logFileDirectory)
      try {
        directoryPath.list() foreach { filename =>
          if (regex matches filename) {
            logging.debug(s"File $filename match regex! Observing it...")

            val b: BufferedReader = files.get(filename) match {
              case Some(bufferedReader) =>
                logging.debug(s"already listening to file ${filename}")
                bufferedReader

              case None =>
                logging.debug(s"creating buffered reader to file ${filename}")

                val fullpath = Paths.get(_service.logFileDirectory, filename)
                val bufferedReader = new BufferedReader(new FileReader(fullpath.toFile))
                files.addOne((filename, bufferedReader))
                var line: String = ""
                while (line != null) {
                  line = bufferedReader.readLine()
                }
                bufferedReader
            }

            var line: String = ""
            while (line != null) {
              line = b.readLine()
              if (line != null) {
                logging.debug(s"Indexing log of Service(${_service.id}, ${_service.name}). Log: ${line}")

                logService.create(LogServiceModel(_service.id, _service.name, filename, line, System.currentTimeMillis()))
              }
            }
          } else {
            logging.debug(s"Filename $filename does not match the regex, skipping...")
          }
        }
      } catch {
        case e: Exception =>
          logging.warn(s"Error collecting logs. Message: ${e.getMessage}")
      }
    }
  }

  def reload(s: Service): Unit = {
    this._service = s
    files.foreach { case (filename, b) =>
      b.close()
      files.remove(filename)
    }
  }

  def stop(): Unit = {
    interval.cancel(true)
    files.foreach { case (filename, b) =>
      b.close()
      files.remove(filename)
    }
  }

  logging.info(s"Starting Logs Observer for Service(${_service.id}, ${_service.name})")
  private val interval = ex.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS)
}

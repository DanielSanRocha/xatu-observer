package com.danielsanrocha.xatu

import com.danielsanrocha.xatu.services.{APIService, ContainerService, ServiceService}
import com.twitter.util.logging.Logger
import scalaj.http.{Http, HttpOptions}

import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import scala.concurrent.ExecutionContext

class TelegramNotifier(
    token: String,
    chatId: String,
    implicit val containerService: ContainerService,
    implicit val apiService: APIService,
    implicit val serviceService: ServiceService,
    implicit val ec: ExecutionContext
) {
  private lazy val logging: Logger = Logger(this.getClass)

  private val ex = new ScheduledThreadPoolExecutor(1)

  val task: Runnable = () => {
    logging.info("Searching for containers,services and apis unhealthy to notify...")

    containerService.getAll(1000, 0) map { containers =>
      containers map { cont =>
        logging.debug(s"Container ${cont.name} status: ${cont.status}")
        if (cont.status == 'F') notify(s"Container ${cont.name} is not running!")
      }
    } recover { case e: Exception =>
      logging.error(s"Error searching for containers to notify. Message: ${e.getMessage}")
    }

    apiService.getAll(1000, 0) map { apis =>
      apis map { api =>
        logging.debug(s"API ${api.name} status: ${api.status}")
        if (api.status == 'F') notify(s"API ${api.name} is broken!")
      }
    } recover { case e: Exception =>
      logging.error(s"Error searching for containers to notify. Message: ${e.getMessage}")
    }

    serviceService.getAll(1000, 0) map { services =>
      services map { s =>
        logging.debug(s"Service ${s.name} status: ${s.status}")
        if (s.status == 'F') notify(s"Service ${s.name} is not running!")
      }
    } recover { case e: Exception =>
      logging.error(s"Error searching for containers to notify. Message: ${e.getMessage}")
    }
  }

  private def notify(message: String): Unit = {
    logging.debug(s"TelegramNotifier message: $message")
    val route = s"https://api.telegram.org/bot$token/sendMessage";

    val result = Http(route)
      .param("chat_id", chatId)
      .param("text", message)
      .option(HttpOptions.connTimeout(10000))
      .option(HttpOptions.readTimeout(10000))
      .execute()

    if (result.code != 200) {
      throw new Exception("Error sending message to telegram!")
    }
  }

  var interval: Option[ScheduledFuture[_]] = None

  def start(): Unit = {
    logging.debug("Starting TelegramNotifier!")
    interval = Some(ex.scheduleAtFixedRate(task, 20, 60, TimeUnit.SECONDS))
  }
}

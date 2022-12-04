package com.danielsanrocha.xatu

import com.danielsanrocha.xatu.models.internals.Status
import com.danielsanrocha.xatu.services.{APIService, ContainerService, ServiceService}
import com.twitter.util.logging.Logger
import scalaj.http.{Http, HttpOptions}

import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}

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
    containerService.getAll(1000, 0) map { containers =>
      containers map { cont =>
        if (cont.status == 'F') notify(s"Container ${cont.name} is not running!")
      }
    }

    apiService.getAll(1000, 0) map { apis =>
      apis map { api =>
        if (api.status == 'F') notify(s"API ${api.name} is broken!")
      }
    }

    serviceService.getAll(1000, 0) map { services =>
      services map { s =>
        if (s.status == 'F') notify(s"Service ${s.name} is not running!")
      }
    }
  }

  def notify(message: String): Unit = {
    logging.error(s"TelegramNotifier message: $message")
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
    interval = Some(ex.scheduleAtFixedRate(task, 60, 200, TimeUnit.SECONDS))
  }
}

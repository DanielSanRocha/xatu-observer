package com.danielsanrocha.xatu.repositories

import com.danielsanrocha.xatu.exceptions.BadArgumentException
import com.danielsanrocha.xatu.models.internals.{Log, LogContainer, LogService}
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.util.logging.Logger
import com.typesafe.config.{Config, ConfigFactory}
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future
import scala.io.Source
import scala.language.postfixOps

class LogRepositoryImpl(config: String, implicit val ec: scala.concurrent.ExecutionContext) extends LogRepository {
  private val logging: Logger = Logger(this.getClass)
  private val jsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build();

  logging.info("Loading configuration file and acessing it...")
  private implicit val conf: Config = ConfigFactory.load()

  private val esHost = conf.getString(s"$config.host")
  private val esPort = conf.getString(s"$config.port")
  private val esIndex = conf.getString(s"$config.index")

  private val indexMapping = Source.fromResource("logIndex.json").mkString

  def createIndex(): Future[Unit] = {
    Future {
      try {
        val route = s"$esHost:$esPort/$esIndex"
        logging.info(s"Creating index at route $route")
        val result = Http(route)
          .option(HttpOptions.connTimeout(10000))
          .option(HttpOptions.readTimeout(10000))
          .header("content-type", "application/json")
          .put(indexMapping)
          .execute()

        if (result.code != 200) {
          throw new Exception(s"ES returned status ${result.code}!")
        }

      } catch {
        case e: Exception =>
          logging.warn(s"Error creating index ${esIndex} on elasticsearch. Error: ${e.getMessage}")
          throw e
      }
    }
  }

  override def create(documentId: String, log: LogService): Future[Unit] = {
    Future {
      val route = s"$esHost:$esPort/$esIndex/_doc/$documentId"
      logging.debug(s"Indexing log on route $route...")
      val result = Http(route)
        .option(HttpOptions.connTimeout(10000))
        .option(HttpOptions.readTimeout(10000))
        .header("content-type", "application/json")
        .put(jsonMapper.writeValueAsString(log))
        .execute()

      if (result.code != 201) {
        logging.error(s"ES Response: ${result.body}")
        throw new Exception(s"Elasticsearch returned status ${result.code} while indexing document with name $documentId")
      }
    }
  }

  override def create(documentId: String, log: LogContainer): Future[Unit] = {
    Future {
      val route = s"$esHost:$esPort/$esIndex/_doc/$documentId"
      logging.debug(s"Indexing log on route $route...")
      val result = Http(route)
        .option(HttpOptions.connTimeout(10000))
        .option(HttpOptions.readTimeout(10000))
        .header("content-type", "application/json")
        .put(jsonMapper.writeValueAsString(log))
        .execute()

      if (result.code != 200) {
        logging.error(s"ES Response: ${result.body}")
        throw new Exception(s"Elasticsearch returned status ${result.code} while indexing document with name $documentId")
      }
    }
  }

  override def search(query: String): Future[Seq[Log]] = {
    Future {
      if (query.contains("\"")) {
        throw new BadArgumentException("Invalid query contains \"")
      }

      val route = s"$esHost:$esPort/$esIndex/_search"
      logging.debug(s"Making a search for logs with query `$query`")

      val data = s"{\"sort\":[{\"created_at\":{\"order\": \"asc\"}}], \"query\": {\"query_string\" : {\"query\": \"$query\"}}}"

      logging.debug(s"Search post data: $data")

      val result = Http(route)
        .header("Content-Type", "application/json")
        .postData(data)
        .option(HttpOptions.connTimeout(10000))
        .option(HttpOptions.readTimeout(10000))
        .execute()

      if (result.code != 200) {
        throw new Exception(s"Elasticsearch returned status ${result.code} while searching with query `${query}`")
      }

      logging.debug(s"ES search response: ${result.body}")

      val body = ujson.read(result.body)

      body("hits")("hits").arr map { hit =>
        val source = hit("_source")
        try {
          val service_id = source("service_id").toString
          LogService(
            service_id.toLong,
            source("service_name").str,
            source("filename").str,
            source("message").str,
            source("created_at").toString().toLong
          )
        } catch {
          case _: NoSuchElementException =>
            LogContainer(source("container_id").toString.toLong, source("container_name").str, source("message").str, source("created_at").toString().toLong)
          case e: Exception => throw e
        }
      } toSeq
    }
  }

  override def status(): Future[Unit] = {
    val route = s"$esHost:$esPort/$esIndex"
    logging.debug(s"Checking index on $route...")

    Future {
      val result = Http(route)
        .option(HttpOptions.connTimeout(10000))
        .option(HttpOptions.readTimeout(10000))
        .execute()

      if (result.code != 200) throw new Exception(s"Elasticsearch response wrong status code (${result.code}) expected 200.")
    }
  }
}

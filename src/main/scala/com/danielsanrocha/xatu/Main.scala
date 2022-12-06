package com.danielsanrocha.xatu

import com.twitter.util.logging.Logger
import slick.jdbc.MySQLProfile.api._

import scala.io.Source
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps
import com.danielsanrocha.xatu.commons.Security
import com.danielsanrocha.xatu.repositories.{LogRepository, LogRepositoryImpl}
import org.apache.log4j.BasicConfigurator

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  BasicConfigurator.configure()

  private val usage = """
  Usage

  start: Start the server
  createTables: Create tables on the database
  createIndex: Create ES index for logs
  hash <password>: Hash a password

  """
  private val logging: Logger = Logger(this.getClass)

  if (args.length == 0) {
    println(usage)
  } else {
    logging.info("Loading execution context...")
    val executorService = Executors.newFixedThreadPool(50)
    val ec: ExecutionContext = ExecutionContext.fromExecutor(executorService)

    logging.info("Loading slick MySQLClient...")
    implicit val client: Database = Database.forConfig("mysql")

    logging.info("Creating logs repository...")
    implicit val logRepository: LogRepository = new LogRepositoryImpl("elasticsearch", ec)

    args(0) match {
      case "start" =>
        val server = new XatuServer()
        server.main(args)

      case "createTables" =>
        val userQuery = Source.fromResource("queries/CreateUsersTable.sql").mkString
        Await.result(client.run(sqlu"#$userQuery"), Duration.Inf)

        val serviceQuery = Source.fromResource("queries/CreateServicesTable.sql").mkString
        Await.result(client.run(sqlu"#$serviceQuery"), Duration.Inf)

        val APIQuery = Source.fromResource("queries/CreateAPIsTable.sql").mkString
        Await.result(client.run(sqlu"#$APIQuery"), Duration.Inf)

        val containerQuery = Source.fromResource("queries/CreateContainersTable.sql").mkString
        Await.result(client.run(sqlu"#$containerQuery"), Duration.Inf)

      case "createIndex" =>
        Await.result(logRepository.createIndex(), atMost = 10 second)

      case "hash" =>
        args.length match {
          case 2 => println(s"Hash: ${Security.hash(args(1))}")
          case _ => println("Missing parameters or too much parameters to function hash. Ex: java -jar main.jar hash <password>")
        }

      case _ => println(usage)
    }
  }
}

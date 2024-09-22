package com.danielsanrocha.xatu

import com.typesafe.scalalogging.Logger
import slick.jdbc.MySQLProfile.api._

import scala.io.Source
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import com.danielsanrocha.xatu.commons.Security
import com.danielsanrocha.xatu.repositories.{LogRepository, LogRepositoryImpl}
import scala.concurrent.ExecutionContext.Implicits.{global => ec}

object Main extends App {
  private val usage = """
  Usage

  start: Start the server.
  createTables: Create tables on the database.
  createIndex: Create ES index for logs.
  createUser: Create an user, you will be prompt for the info.

  """
  private val logging = Logger(this.getClass)
  logging.info("Starting the application...")

  logging.error("Testing logging.error")
  logging.warn("Testing logging.warn")
  logging.info("Testing logging.info")
  logging.debug("Testing logging.debug")
  logging.trace("Testing logging.trace")

  if (args.length == 0) {
    println(usage)
  } else {
    logging.info("Loading slick MySQLClient...")
    implicit val client: Database = Database.forConfig("mysql")

    logging.info("Creating logs repository...")
    implicit val logRepository: LogRepository = new LogRepositoryImpl("elasticsearch", ec)

    args(0) match {
      case "start" =>
        logging.info(s"Instantiating the great manager...")
        implicit val greatManager: TheGreatManager = new TheGreatManager()
        greatManager.start()
        logging.info(s"Starting API...")
        val server = new XatuServer()
        server.main(args)

      case "createTables" =>
        logging.info("Creating tb_users table...")
        val userQuery = Source.fromResource("queries/CreateUsersTable.sql").mkString
        Await.result(client.run(sqlu"#$userQuery"), atMost = 10 second)

        logging.info("Creating tb_services table...")
        val serviceQuery = Source.fromResource("queries/CreateServicesTable.sql").mkString
        Await.result(client.run(sqlu"#$serviceQuery"), atMost = 10 second)

        logging.info("Creating tb_apis table...")
        val APIQuery = Source.fromResource("queries/CreateAPIsTable.sql").mkString
        Await.result(client.run(sqlu"#$APIQuery"), atMost = 10 second)

        logging.info("Creating tb_containers table...")
        val containerQuery = Source.fromResource("queries/CreateContainersTable.sql").mkString
        Await.result(client.run(sqlu"#$containerQuery"), atMost = 10 second)

      case "createIndex" =>
        logging.info("Creating index...")
        Await.result(logRepository.createIndex(), atMost = 10 second)

      case "createUser" =>
        val stdin = System.console()
        println("Enter username:")
        val name = stdin.readLine()
        println("Enter email:")
        val email = stdin.readLine()
        println("Enter password:")
        val password = stdin.readPassword().mkString
        println("Confirm password:")
        val confirmPassword = stdin.readPassword().mkString

        if (password != confirmPassword) {
          throw new Exception("Password did not match!")
        }

        logging.info("Creating user...")
        Await.result(client.run(sqlu"INSERT INTO tb_users (name,email,password) VALUES ($name,$email,${Security.hash(password)});"), atMost = 10 second)
        logging.info("Created!")

      case _ => println(usage)
    }
  }
}

package com.danielsanrocha.xatu

import com.danielsanrocha.xatu.commons.Security
import com.twitter.util.logging.Logger
import slick.jdbc.MySQLProfile.api._

import scala.io.Source
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  private val usage = """
  Usage

  start: Start the server
  createTables: Create tables on the database
  hash <password>: Hash a password

  """
  private val logging: Logger = Logger(this.getClass)

  if (args.length == 0) {
    println(usage)
  } else {
    logging.info("Loading slick MySQLClient...")
    implicit val client: Database = Database.forConfig("mysql")

    args(0) match {
      case "start" => {
        val server = new XatuServer()
        server.main(args)
      }
      case "createTables" => {
        val userQuery = Source.fromResource("queries/CreateUsersTable.sql").mkString
        Await.result(client.run(sqlu"#$userQuery"), Duration.Inf)

        val serviceQuery = Source.fromResource("queries/CreateServicesTable.sql").mkString
        Await.result(client.run(sqlu"#$serviceQuery"), Duration.Inf)

        val APIQuery = Source.fromResource("queries/CreateAPIsTable.sql").mkString
        Await.result(client.run(sqlu"#$APIQuery"), Duration.Inf)
      }
      case "hash" => {
        args.length match {
          case 2 => println(s"Hash: ${Security.hash(args(1))}")
          case _ => println("Missing parameters or too much parameters to function hash. Ex: java -jar main.jar hash <password>")
        }
      }
      case _ => println(usage)
    }
  }
}

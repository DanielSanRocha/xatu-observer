package com.danielsanrocha.xatu

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
      case _ => println(usage)
    }
  }
}

package com.danielsanrocha.xatu.repositories

import slick.jdbc.MySQLProfile

import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import slick.jdbc.MySQLProfile.api._

trait TestRepository {
  protected implicit val client: MySQLProfile.backend.Database = Database.forConfig("test.h2mem")

  def runQuery(query: String): Unit = {
    Await.result(client.run(sqlu"#$query"), 10 second)
  }

  private val userQuery = Source.fromResource("queries/CreateUsersTable.sql").mkString
  runQuery(userQuery)

  private val serviceQuery = Source.fromResource("queries/CreateServicesTable.sql").mkString
  runQuery(serviceQuery)

  private val APIQuery = Source.fromResource("queries/CreateAPIsTable.sql").mkString
  runQuery(APIQuery)

  private val containerQuery = Source.fromResource("queries/CreateContainersTable.sql").mkString
  runQuery(containerQuery)

}

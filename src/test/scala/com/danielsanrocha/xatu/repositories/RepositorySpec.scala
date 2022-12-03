package com.danielsanrocha.xatu.repositories

import com.danielsanrocha.xatu.UnitSpec
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class RepositorySpec extends UnitSpec {
  protected implicit val client = Database.forConfig("test.h2mem")

  def runQuery(query: String): Unit = {
    Await.result(client.run(sqlu"#$query"), Duration.Inf)
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

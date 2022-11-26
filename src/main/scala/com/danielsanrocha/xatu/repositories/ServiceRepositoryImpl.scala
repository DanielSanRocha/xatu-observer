package com.danielsanrocha.xatu.repositories

import scala.language.postfixOps
import scala.concurrent.Future
import java.sql.Timestamp
import slick.jdbc.MySQLProfile.api._

import com.danielsanrocha.xatu.models.internals.Service
import com.danielsanrocha.xatu.commons.FutureConverters.{RichScalaFuture}

class ServiceRepositoryImpl(implicit client: Database, implicit val ec: scala.concurrent.ExecutionContext) extends ServiceRepository {
  class ServiceTable(tag: Tag) extends Table[Service](tag, "tb_services") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def logfileRegex = column[String]("log_file_regex")
    def pidfile = column[String]("pidfile")
    def createDate = column[Timestamp]("create_date")
    def updateDate = column[Timestamp]("update_date")

    def * = (id, name, logfileRegex, pidfile, createDate, updateDate) <> (Service.tupled, Service.unapply)
  }

  lazy val services = TableQuery[ServiceTable]

  override def getById(id: Long): Future[Option[Service]] = {
    client.run(services.take(1).filter(_.id === id).result.headOption)
  }
}

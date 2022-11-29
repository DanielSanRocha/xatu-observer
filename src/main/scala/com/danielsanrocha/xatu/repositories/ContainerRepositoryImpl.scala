package com.danielsanrocha.xatu.repositories

import com.danielsanrocha.xatu.models.internals.{Container, NewContainer}
import slick.jdbc.MySQLProfile.api._

import java.sql.Timestamp
import scala.concurrent.Future
import scala.language.postfixOps

class ContainerRepositoryImpl(implicit client: Database, implicit val ec: scala.concurrent.ExecutionContext) extends ContainerRepository {
  class ContainerTable(tag: Tag) extends Table[Container](tag, "tb_containers") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def createDate = column[Timestamp]("create_date")
    def updateDate = column[Timestamp]("update_date")

    def * = (id, name, createDate, updateDate) <> (Container.tupled, Container.unapply)
  }

  lazy val containers = TableQuery[ContainerTable]

  override def getById(id: Long): Future[Option[Container]] = {
    client.run(containers.filter(_.id === id).result.headOption)
  }

  override def create(nc: NewContainer): Future[Long] = {
    client.run((containers.map(s => (s.name)) returning containers.map(_.id)) += ((nc.name)))
  }

  override def delete(id: Long): Future[Boolean] = {
    client.run(containers.filter(_.id === id).delete)
  } map {
    case 0 => false
    case _ => true
  }

  override def getAll(limit: Long, offset: Long): Future[Seq[Container]] = {
    client.run(containers.take(limit).drop(offset).result)
  }
}

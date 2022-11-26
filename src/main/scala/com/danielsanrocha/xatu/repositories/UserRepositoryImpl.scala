package com.danielsanrocha.xatu.repositories

import scala.language.postfixOps
import com.twitter.util.Future
import java.sql.Timestamp
import com.danielsanrocha.xatu.models.internals.User
import slick.jdbc.MySQLProfile.api._

import com.danielsanrocha.xatu.models.internals.User
import com.danielsanrocha.xatu.commons.FutureConverters.{RichScalaFuture}

class UserRepositoryImpl(implicit client: Database) extends UserRepository {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  class UserTable(tag: Tag) extends Table[User](tag, "tb_users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def password = column[String]("password")
    def createDate = column[Timestamp]("create_date")
    def updateDate = column[Timestamp]("update_date")

    def * = (id, name, email, password, createDate, updateDate) <> (User.tupled, User.unapply)
  }

  lazy val users = TableQuery[UserTable]

  override def getById(id: Long): Future[Option[User]] = {
    client.run(users.take(1).filter(_.id === id).result.headOption) asTwitter
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    client.run(users.take(1).filter(_.email === email).result.headOption) asTwitter
  }
}

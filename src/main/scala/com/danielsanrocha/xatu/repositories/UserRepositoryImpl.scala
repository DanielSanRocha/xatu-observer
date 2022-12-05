package com.danielsanrocha.xatu.repositories

import scala.language.postfixOps
import scala.concurrent.Future
import java.sql.Timestamp
import com.danielsanrocha.xatu.models.internals.User
import slick.jdbc.MySQLProfile.api._
import com.danielsanrocha.xatu.models.internals.User
import com.danielsanrocha.xatu.commons.FutureConverters.RichScalaFuture
import com.twitter.util.logging.Logger

class UserRepositoryImpl(implicit client: Database, implicit val ec: scala.concurrent.ExecutionContext) extends UserRepository {
  private val logging: Logger = Logger(this.getClass)

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
    client.run(users.filter(_.id === id).result.headOption)
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    logging.debug(s"Searching for user with email $email")
    client.run(users.filter(_.email === email).result.headOption) map {
      case Some(user) =>
        logging.info("Found user by emai;!")
        Some(user)
      case None =>
        logging.debug("User not found by email!")
        None
    }
  }
}

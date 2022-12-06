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
    logging.debug(s"Searching for user with by id $id")
    val start = System.currentTimeMillis
    client.run(users.filter(_.id === id).result.headOption) map {
      case Some(user) =>
        val end = System.currentTimeMillis
        val time = (end - start).toFloat / 1000
        logging.debug(s"User getById took $time with id $id to find a user")
        Some(user)
      case None =>
        val end = System.currentTimeMillis
        val time = (end - start).toFloat / 1000
        logging.debug(s"User getById took $time with id $id to not find a user")
        None
    }
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    logging.debug(s"Searching for user with email $email")
    val start = System.currentTimeMillis
    client.run(users.filter(_.email === email).result.headOption) map {
      case Some(user) =>
        val end = System.currentTimeMillis
        val time = (end - start).toFloat / 1000
        logging.debug(s"getByEmail took $time seconds to find user with email $email")
        logging.info("Found user by email!")
        Some(user)
      case None =>
        val end = System.currentTimeMillis
        val time = (end - start).toFloat / 1000
        logging.debug(s"getByEmail took $time seconds to not find a user with email $email")
        logging.debug("User not found by email!")
        None
    }
  }
}

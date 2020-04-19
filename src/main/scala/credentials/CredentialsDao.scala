package credentials

import com.typesafe.scalalogging.Logger
import db.DatabaseError

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import cats.data.OptionT
import cats.data.EitherT
import cats.effect.Bracket

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

trait CredentialsDao[F[_]] {
  def storeCredentials(
      credentials: EncodedCredentials
  ): F[Int]

  def getCredentials(uid: Int): OptionT[F, EncodedCredentials]
}

class DoobieCredentialsDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends CredentialsDao[F] {

  /**
    * @param credentials to store. password must be encoded.
    * @return the number of affected rows. If 0, the insert was
    * not successful.
    */
  def storeCredentials(
      credentials: EncodedCredentials
  ): F[Int] = CredentialsSql.insert(credentials).transact(xa)

  def getCredentials(uid: Int): OptionT[F, EncodedCredentials] = OptionT(
    CredentialsSql.select(uid).transact(xa)
  )
}

object CredentialsSql {
  def insert(cr: EncodedCredentials): ConnectionIO[Int] = {
    sql"""
         | INSERT IGNORE INTO credentials (uid, password)
         | VALUES (${cr.uid}, ${cr.encodedPassword})
         |""".stripMargin.update.run
  }

  def select(uid: Int): ConnectionIO[Option[EncodedCredentials]] = {
    sql"""
         | SELECT uid, password
         | FROM user
         | WHERE uid = $uid
         |""".stripMargin.query[EncodedCredentials].option
  }
}

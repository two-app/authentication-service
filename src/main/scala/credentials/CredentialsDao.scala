package credentials

import com.typesafe.scalalogging.Logger
import db.DatabaseError
import db.ctx._

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

trait CredentialsDao {
  def storeCredentials(credentials: EncodedCredentials): Future[Option[DatabaseError]]
}

class QuillCredentialsDao extends CredentialsDao {
  val logger: Logger = Logger(classOf[CredentialsDao])

  override def storeCredentials(credentials: EncodedCredentials): Future[Option[DatabaseError]] = {
    val record = CredentialsRecord(credentials)
    logger.info(s"Storing credentials: $record.")
    run(quote {
      querySchema[CredentialsRecord]("credentials").insert(lift(record))
    }).map(_ => None).recover {
      case t: Throwable =>
        logger.error(s"Severe error: failed to store user '${record.uid}' credentials.", t)
        Option(DatabaseError.Other())
    }
  }
}

final case class CredentialsRecord(uid: Int, password: String)

object CredentialsRecord {
  def apply(credentials: EncodedCredentials): CredentialsRecord = {
    new CredentialsRecord(credentials.uid, credentials.encodedPassword)
  }
}

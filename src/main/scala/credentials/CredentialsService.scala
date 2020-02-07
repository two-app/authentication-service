package credentials

import response.ErrorResponse
import response.ErrorResponse.InternalError

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

trait CredentialsService {
  def storeCredentials(credentials: EncodedCredentials): Future[Option[ErrorResponse]]
}

class CredentialsServiceImpl(credentialsDao: CredentialsDao) extends CredentialsService {
  override def storeCredentials(credentials: EncodedCredentials): Future[Option[ErrorResponse]] = {
    credentialsDao.storeCredentials(credentials).map(maybeError => {
      maybeError.map(_ => InternalError())
    })
  }
}

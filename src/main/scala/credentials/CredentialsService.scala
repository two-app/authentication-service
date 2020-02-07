package credentials

import response.ErrorResponse
import response.ErrorResponse.InternalError
import tokens.Tokens

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

sealed trait CredentialsService {
  def storeCredentials(credentials: EncodedCredentials): Future[Either[ErrorResponse, Tokens]]
}

class CredentialsServiceImpl(credentialsDao: CredentialsDao) extends CredentialsService {
  override def storeCredentials(credentials: EncodedCredentials): Future[Either[ErrorResponse, Tokens]] = {
    credentialsDao.storeCredentials(credentials).map(maybeError => {
      maybeError.map(_ => InternalError()).toLeft(Tokens(credentials.uid, None, None))
    })
  }
}

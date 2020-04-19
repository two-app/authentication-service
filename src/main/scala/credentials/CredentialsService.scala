package credentials

import response.ErrorResponse
import response.ErrorResponse.InternalError
import tokens.Tokens

import cats.data.EitherT
import cats.Monad

trait CredentialsService[F[_]] {
  def storeCredentials(
      credentials: EncodedCredentials
  ): EitherT[F, ErrorResponse, Tokens]
}

class CredentialsServiceImpl[F[_]: Monad](
    credentialsDao: CredentialsDao[F]
) extends CredentialsService[F] {

  override def storeCredentials(
      credentials: EncodedCredentials
  ): EitherT[F, ErrorResponse, Tokens] = {
    EitherT
      .right(credentialsDao.storeCredentials(credentials))
      .flatMap((affectedRows: Int) =>
        EitherT.cond(
          affectedRows == 1,
          Tokens(credentials.uid, None, None),
          InternalError()
        )
      )
  }
}

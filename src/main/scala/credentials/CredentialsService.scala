package credentials

import response.ErrorResponse
import response.ErrorResponse.InternalError
import tokens.Tokens

import cats.implicits._
import cats.data.EitherT
import cats.Monad
import user.UserService
import response.ErrorResponse.NotFoundError
import response.ErrorResponse.ClientError
import user.User

trait CredentialsService[F[_]] {
  def storeCredentials(
      credentials: EncodedCredentials
  ): EitherT[F, ErrorResponse, Tokens]

  def loginWithCredentials(
      credentials: LoginCredentials
  ): EitherT[F, ErrorResponse, Tokens]
}

class CredentialsServiceImpl[F[_]: Monad](
    userService: UserService[F],
    credentialsDao: CredentialsDao[F]
) extends CredentialsService[F] {

  override def storeCredentials(
      credentials: EncodedCredentials
  ): EitherT[F, ErrorResponse, Tokens] = {
    EitherT.right(credentialsDao.storeCredentials(credentials))
      .flatMap((affectedRows: Int) =>
        EitherT.cond(
          affectedRows == 1,
          Tokens(credentials.uid, None, None),
          InternalError()
        )
      )
  }

  override def loginWithCredentials(
      loginCreds: LoginCredentials
  ): EitherT[F, ErrorResponse, Tokens] = {
    for {
      user <- userService.getUser(loginCreds.email)
      encodedCreds <- this.getCredentials(user)
      tokens <- this.verifyLoginAndGenerateTokens(user, loginCreds, encodedCreds)
    } yield tokens
  }

  private def getCredentials(
    user: User
  ): EitherT[F, ErrorResponse, EncodedCredentials] =
    credentialsDao.getCredentials(user.uid).toRight(
        InternalError(s"Failed to find credentials for user with UID ${user.uid}")
    ).leftWiden[ErrorResponse]

  private def verifyLoginAndGenerateTokens(
    user: User,
    loginCreds: LoginCredentials,
    encodedCreds: EncodedCredentials
  ): EitherT[F, ErrorResponse, Tokens] =
    EitherT.cond[F](
      CredentialsMatcher.matches(loginCreds.rawPassword, encodedCreds.encodedPassword),
      Tokens(user.uid, user.pid, user.cid),
      ClientError("Invalid email/password combination.")
    ).leftWiden[ErrorResponse]

}

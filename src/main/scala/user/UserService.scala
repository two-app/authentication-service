package user

import cats.implicits._
import response.ErrorResponse
import response.ErrorResponse.NotFoundError
import response.ErrorResponse.ClientError
import cats.data.EitherT
import cats.Monad

trait UserService[F[_]] {
  def getUser(email: String): EitherT[F, ErrorResponse, User]
}

class UserServiceImpl[F[_]: Monad](userDao: UserDao[F]) extends UserService[F] {
  override def getUser(email: String): EitherT[F, ErrorResponse, User] = {
    validateEmail(email).flatMap(_ =>
      userDao.getUser(email).toRight(NotFoundError("User does not exist."))
    )
  }

  private def validateEmail(email: String): EitherT[F, ErrorResponse, Unit] =
    EitherT.cond[F](
      EmailValidator.isValid(email),
      (),
      ClientError("Invalid email.")
    ).leftWiden[ErrorResponse]
}

package tokens

import cats.data.EitherT
import response.ErrorResponse
import user.UserService
import cats.Monad

trait TokenService[F[_]] {

  def refreshAccessToken(
      refreshToken: String
  ): EitherT[F, ErrorResponse, String]

}

class TokenServiceImpl[F[_]: Monad](userService: UserService[F])
    extends TokenService[F] {

  override def refreshAccessToken(
      refreshToken: String
  ): EitherT[F, ErrorResponse, String] = {
    for {
      uid <- EitherT.fromEither(RefreshToken.decode(refreshToken))
      user <- userService.getUser(uid)
    } yield AccessToken.from(user.uid, user.pid, user.cid)
  }

}

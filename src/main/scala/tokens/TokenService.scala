package tokens

import cats.data.EitherT
import response.ErrorResponse
import user.UserService

trait TokenService[F[_]] {

  def refreshAccessToken(
      refreshToken: String
  ): EitherT[F, ErrorResponse, String]

}

class TokenServiceImpl[F[_]](userService: UserService[F])
    extends TokenService[F] {

  override def refreshAccessToken(
      refreshToken: String
  ): EitherT[F, ErrorResponse, String] = ???

}

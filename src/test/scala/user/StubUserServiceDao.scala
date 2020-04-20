package user

import cats.data.OptionT
import cats.Applicative

class StubUserServiceDao[F[_]: Applicative] extends UserDao[F] {

  var getUserResponse: Option[User] = None

  override def getUser(email: String): OptionT[F,User] = OptionT.fromOption(getUserResponse)
  
}

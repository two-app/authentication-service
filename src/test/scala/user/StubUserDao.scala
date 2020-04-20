package user

import cats.effect.IO
import cats.data.OptionT
import scala.collection.mutable

class StubUserDao extends UserDao[IO] {

  val userEmailMap: mutable.Map[String, User] = mutable.Map()
  override def getUser(email: String): OptionT[IO,User] = OptionT.fromOption(userEmailMap.get(email))
  
}

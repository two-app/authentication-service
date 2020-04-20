package user

import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

final case class User(
    uid: Int,
    pid: Option[Int],
    cid: Option[Int],
    firstName: String,
    lastName: String
)

object User {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat5(User.apply)
}

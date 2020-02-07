package credentials

import com.typesafe.scalalogging.Logger
import spray.json.{JsNumber, JsString, JsValue, RootJsonFormat}
import spray.json.DefaultJsonProtocol._

final case class ModelValidationError(reason: String)

final case class UserCredentials(uid: Int, password: String)

object UserCredentials {

  implicit object UserCredentialsFormat extends RootJsonFormat[Either[ModelValidationError, UserCredentials]] {
    override def read(json: JsValue): Either[ModelValidationError, UserCredentials] = {
      val f = json.asJsObject.fields
      UserCredentials.from(extractInt(f, "uid"), extractString(f, "password"))
    }

    override def write(obj: Either[ModelValidationError, UserCredentials]): JsValue = null

    def extractString(f: Map[String, JsValue], k: String): String = f.getOrElse(k, JsString.empty).convertTo[String]

    def extractInt(f: Map[String, JsValue], k: String): Int = f.getOrElse(k, JsNumber(-1)).convertTo[Int]
  }

  def from(uid: Int, password: String): Either[ModelValidationError, UserCredentials] = {
    if (uid < 1) return Left(ModelValidationError("UID must be greater than zero."))
    if (password.length < 6) return Left(ModelValidationError("Password must be at least six characters in length."))
    Right(UserCredentials(uid, password))
  }
}
package credentials

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import config.MasterRoute
import credentials.UserCredentials
import db.FlywayHelper
import scala.reflect.ClassTag
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
class CredentialsRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach {

  val route: Route = MasterRoute.credentialsRoute

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  def PostCredentials[T: FromEntityUnmarshaller: ClassTag](
      userCredentials: UserCredentials
  ): T = {
    implicit val UCF: RootJsonFormat[UserCredentials] =
      jsonFormat2(UserCredentials.apply)
    Post("/credentials", userCredentials) ~> route ~> check {
      entityAs[T]
    }
  }

  describe("POST /credentials") {}
}
//   "POST /credentials with valid credentials" should "return 200 OK" in {
//     postCredentials(1, "testPassword") ~> route ~> check {
//       response.status shouldBe StatusCodes.OK
//       val fields = entityAs[String].parseJson.asJsObject.fields

//       fields.contains("accessToken") shouldBe true
//       fields("accessToken").convertTo[String].length should be > 0

//       fields.contains("refreshToken") shouldBe true
//       fields("refreshToken").convertTo[String].length should be > 0
//     }
//   }

//   "POST /credentials with an invalid UID" should "return a 400 Bad Request" in {
//     postCredentials(-1, "testPassword") ~> route ~> check {
//       response.status shouldBe StatusCodes.BadRequest
//       entityAs[String] shouldBe """{"status":"400 Bad Request","reason":"UID must be greater than zero."}"""
//     }
//   }

//   "POST /credentials with a short password" should "return a 400 Bad Request" in {
//     postCredentials(1, "short") ~> route ~> check {
//       response.status shouldBe StatusCodes.BadRequest
//       entityAs[String] shouldBe """{"status":"400 Bad Request","reason":"Password must be at least six characters in length."}"""
//     }
//   }

//   def postCredentials(uid: Int, password: String): HttpRequest = {
//     val credentials = s"""{"uid": $uid, "password": "$password"}"""
//     Post("/credentials").withEntity(ContentTypes.`application/json`, credentials)
//   }

// }

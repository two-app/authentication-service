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
import db.DatabaseTestMixin
import scala.reflect.ClassTag
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import tokens.Tokens
import request.RequestTestArbitraries
import response.ErrorResponse
import response.ErrorResponse.{ClientError, InternalError}

class CredentialsRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with RequestTestArbitraries
    with DatabaseTestMixin {

  val route: Route = new MasterRoute(xa).credentialsRoute

  override def beforeEach(): Unit = cleanMigrate()

  def PostCredentials[T: FromEntityUnmarshaller: ClassTag](
      userCredentials: UserCredentials
  ): T = {
    implicit val UCF: RootJsonFormat[UserCredentials] =
      jsonFormat2(UserCredentials.apply)
    Post("/credentials", userCredentials) ~> route ~> check {
      entityAs[T]
    }
  }

  describe("POST /credentials") {
    it("should return tokens with valid credentials") {
      val creds = UserCredentials(1, "testPassword")
      
      val tokens = PostCredentials[Tokens](creds)

      extractContext(tokens.accessToken).uid shouldBe creds.uid
    }

    it("should return a bad request for a small password") {
      val creds = UserCredentials(1, "bad")

      val error = PostCredentials[ErrorResponse](creds)

      error shouldBe ClientError("Password must be at least six characters in length.")
    }

    it("should return a bad request for a uid of 0") {
      val creds = UserCredentials(0, "testPassword")

      val error = PostCredentials[ErrorResponse](creds)

      error shouldBe ClientError("UID must be greater than zero.")
    }

    it("should return an internal server error for duplicate ids") {
      val creds = UserCredentials(1, "testPassword")

      PostCredentials[Tokens](creds)
      val error = PostCredentials[ErrorResponse](creds)

      error shouldBe InternalError()
    }
  }
}

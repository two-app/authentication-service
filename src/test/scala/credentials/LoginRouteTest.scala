package credentials

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import db.DatabaseTestMixin
import request.RequestTestArbitraries
import scala.reflect.ClassTag
import config.TestServices
import user.UserServiceImpl
import user.StubUserServiceDao
import cats.effect.IO
import response.ErrorResponse
import response.ErrorResponse.ClientError
import user.UserTestArbitraries
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import tokens.Tokens

class LoginRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with RequestTestArbitraries
    with UserTestArbitraries
    with DatabaseTestMixin {

  var stubUserDao: StubUserServiceDao[IO] = _
  var route: Route = _
  override def beforeEach(): Unit = {
    cleanMigrate()
    val services = new TestServices()
    stubUserDao = services.stubUserDao
    route = services.masterRoute
  }

  def PostLogin[T: FromEntityUnmarshaller: ClassTag](
      loginCredentials: LoginCredentials
  ): T = Post("/login", loginCredentials) ~> route ~> check {
    entityAs[T]
  }

  def PostCredentials[T: FromEntityUnmarshaller: ClassTag](
      userCredentials: UserCredentials
  ): T = {
    implicit val UCF: RootJsonFormat[UserCredentials] =
      jsonFormat2(UserCredentials.apply)
    Post("/credentials", userCredentials) ~> route ~> check {
      entityAs[T]
    }
  }

  it("should return tokens for a valid login") {
    val user = arbitraryUser()
    stubUserDao.getUserResponse = Option(user)

    PostCredentials[Tokens](UserCredentials(user.uid, "testPassword"))
    val tokens = PostLogin[Tokens](
      LoginCredentials("test@two.com", "testPassword")
    )

    extractContext(tokens.accessToken).uid shouldBe user.uid
  }

  it("should return a bad request for a malformed email") {
    val response = PostLogin[ErrorResponse](
      LoginCredentials("bla", "testPassword")
    )

    response shouldBe ClientError("Invalid email.")
  }

  it("should return a bad request for an invalid email/password combo") {
    val user = arbitraryUser()
    stubUserDao.getUserResponse = Option(user)

    PostCredentials[Tokens](UserCredentials(user.uid, "testPassword"))
    val response = PostLogin[ErrorResponse](
      LoginCredentials("test@two.com", "invalidPassword")
    )

    response shouldBe ClientError("Invalid email/password combination.")
  }

}

package tokens

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import config.TestServices
import db.FlywayHelper
import request.RequestTestArbitraries
import scala.reflect.ClassTag
import response.ErrorResponse
import response.ErrorResponse.ClientError
import response.ErrorResponse.AuthorizationError
import pdi.jwt.Jwt
import pdi.jwt.JwtClaim
import java.time.Clock
import cats.effect.IO
import user.UserServiceImpl
import user.StubUserServiceDao
import user.UserTestArbitraries

class TokensRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with RequestTestArbitraries
    with UserTestArbitraries {

  var stubUserDao: StubUserServiceDao[IO] = _
  var route: Route = _

  override def beforeEach(): Unit = {
    val services = new TestServices()
    stubUserDao = services.stubUserDao
    route = services.masterRoute
  }

  def PostTokens[T: FromEntityUnmarshaller: ClassTag](
      tokensRequest: TokensRequest
  ): T = Post("/tokens", tokensRequest) ~> route ~> check {
    entityAs[T]
  }

  def PostRefresh[T: FromEntityUnmarshaller: ClassTag](
      refreshToken: String
  ): T =
    Post("/refresh").withHeaders(List(authHeader(refreshToken))) ~> route ~> check {
      entityAs[T]
    }

  describe("POST /tokens") {
    it("should return new tokens") {
      val (uid, pid, cid) = (1, 2, 3)

      val tokens =
        PostTokens[Tokens](TokensRequest(uid, Option(pid), Option(cid)))
      val user = extractContext(tokens.accessToken)

      user.uid shouldBe uid
      user.pid shouldBe Option(pid)
      user.cid shouldBe Option(cid)
    }
  }

  describe("POST /refresh") {
    it("should return a new access token for a valid refresh token and user") {
      val user = arbitraryUser()
      stubUserDao.getUserResponse = Option(user)
      val token = RefreshToken.from(user.uid)

      val response = PostRefresh[String](token)

      println(response)
      response.length should be > 0
    }

    it("should return an authorization error if the user no longer exists.") {
      val token = RefreshToken.from(1)

      PostRefresh[ErrorResponse](token) shouldBe AuthorizationError("Failed to authorize user.")
    }

    it("should return an authorization error with no token") {
      val response = PostRefresh[ErrorResponse]("")

      response shouldBe AuthorizationError("Failed to authorize user.")
    }

    it("should return an authorization error with a badly signed token") {
      val token = Jwt.encode(
        JwtClaim(
          issuer = Option("two"),
          content = s"""{"uid": 5, "role": "REFRESH"}"""
        ).issuedNow(Clock.systemUTC())
      )

      val response = PostRefresh[ErrorResponse](token)

      response shouldBe AuthorizationError("Failed to authorize user.")
    }
  }

}

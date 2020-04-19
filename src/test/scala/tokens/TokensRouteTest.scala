package tokens


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtOptions}
import request.UserContext
import spray.json._

class TokensRouteTest extends AsyncFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  val route: Route = new TokensRouteDispatcher().route

  "POST /tokens without a uid" should "return bad request" in {
    val req = TokensRequest(1, Option(2), Option(3))
    Post("/tokens", req) ~> route ~> check {
      Jwt.decode(entityAs[Tokens].accessToken, JwtOptions(signature = false, expiration = false, notBefore = false))
        .map(claim => claim.content.parseJson.convertTo[UserContext])
        .map(ctx => {
          ctx.uid shouldBe 1
          ctx.pid shouldBe 2
          ctx.cid shouldBe 3
        })
      entityAs[Tokens].refreshToken.isBlank shouldBe false
    }
  }

}

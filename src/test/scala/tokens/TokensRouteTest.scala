package tokens

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import config.MasterRoute
import db.FlywayHelper
import request.RequestTestArbitraries
import scala.reflect.ClassTag

class TokensRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with RequestTestArbitraries {

  val route: Route = MasterRoute.tokensRoute

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  def PostTokens[T: FromEntityUnmarshaller: ClassTag](
      tokensRequest: TokensRequest
  ): T = Post("/tokens", tokensRequest) ~> route ~> check {
    entityAs[T]
  }

  describe("POST /tokens") {
    it("should return new tokens") {
      val (uid, pid, cid) = (1, 2, 3)

      val tokens = PostTokens[Tokens](TokensRequest(uid, Option(pid), Option(cid)))
      val user = extractContext(tokens.accessToken)

      user.uid shouldBe uid
      user.pid shouldBe Option(pid)
      user.cid shouldBe Option(cid)
    }
  }

}

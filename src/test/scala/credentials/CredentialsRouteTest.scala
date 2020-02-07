package credentials

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import db.FlywayHelper
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CredentialsRouteTest extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    val flyway = FlywayHelper.getFlyway
    flyway.clean()
    flyway.migrate()
  }

  val route: Route = new CredentialsRoute(new CredentialsServiceImpl(new QuillCredentialsDao())).route

  "POST /credentials with valid credentials" should "return 200 OK" in {
    postCredentials(1, "testPassword") ~> route ~> check {
      response.status shouldBe StatusCodes.OK
    }
  }

  "POST /credentials with an invalid UID" should "return a 400 Bad Request" in {
    postCredentials(-1, "testPassword") ~> route ~> check {
      response.status shouldBe StatusCodes.BadRequest
      entityAs[String] shouldBe """{"status":"400 Bad Request","reason":"UID must be greater than zero."}"""
    }
  }

  "POST /credentials with a short password" should "return a 400 Bad Request" in {
    postCredentials(1, "short") ~> route ~> check {
      response.status shouldBe StatusCodes.BadRequest
      entityAs[String] shouldBe """{"status":"400 Bad Request","reason":"Password must be at least six characters in length."}"""
    }
  }

  def postCredentials(uid: Int, password: String): HttpRequest = {
    val credentials = s"""{"uid": $uid, "password": "$password"}"""
    Post("/credentials").withEntity(ContentTypes.`application/json`, credentials)
  }

}
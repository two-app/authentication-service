// package credentials

// import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
// import akka.http.scaladsl.server.Route
// import akka.http.scaladsl.testkit.ScalatestRouteTest
// import db.FlywayHelper
// import org.scalatest.BeforeAndAfterEach
// import org.scalatest.concurrent.ScalaFutures
// import org.scalatest.flatspec.AsyncFlatSpec
// import org.scalatest.matchers.should.Matchers
// import spray.json.DefaultJsonProtocol._
// import spray.json._

// class CredentialsRouteTest extends AsyncFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with BeforeAndAfterEach {

//   override def beforeEach(): Unit = {
//     val flyway = FlywayHelper.getFlyway
//     flyway.clean()
//     flyway.migrate()
//   }

//   val route: Route = new CredentialsRoute(new CredentialsServiceImpl(new QuillCredentialsDao())).route

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

package user

import org.scalatest.funspec.AsyncFunSpec
import cats.effect.IO
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import org.scalatest.BeforeAndAfterEach
import db.FlywayHelper
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.HttpEntity
import spray.json._
import akka.http.scaladsl.model.ContentTypes

class UserDaoTest extends AsyncFunSpec with Matchers with BeforeAndAfterEach {

  var client: StubUserServiceClient[IO] = _
  var userDao: UserDao[IO] = _

  override def beforeEach(): Unit = {
    client = new StubUserServiceClient[IO]()
    userDao = new UserServiceDao(client)
  }

  describe("getUser by email") {
    it("should form the correct http request") {
      val email = "test@two.com"
      userDao.getUser(email).value.unsafeRunSync()

      client.lastRequest shouldBe HttpRequest(
        method = HttpMethods.GET,
        uri = s"/user?email=${email}"
      )
    }

    it("should correctly map the response entity to a user") {
      val responseUser =
        User(5, Option(2), Option(3), "First Name", "Last Name")
      val responseJson = responseUser.toJson.compactPrint

      val response = HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`application/json`, responseJson)
      )

      client.response = response
      val maybeUser = userDao.getUser("test@gmail.com").value.unsafeRunSync()

      maybeUser shouldBe Option(responseUser)
    }

    it("should return None for a 404 response") {
      val stubResponse = HttpResponse(
        status = StatusCodes.NotFound
      )

      client.response = stubResponse
      val maybeUser = userDao.getUser("test@gmail.com").value.unsafeRunSync()

      maybeUser shouldBe None
    }

    it("should fail for an internal server error") {
      val stubResponse = HttpResponse(
        status = StatusCodes.InternalServerError
      )

      client.response = stubResponse
      val errorOrUser =
        userDao.getUser("test@gmail.com").value.attempt.unsafeRunSync()

      errorOrUser.isLeft shouldBe true
      errorOrUser.left.get.getMessage() should startWith(
        "Unexpected response from user-service"
      )
    }
  }

  describe("getUser by UID") {
    it("should form the correct http request") {
      val uid = 5
      userDao.getUser(uid).value.unsafeRunSync()

      client.lastRequest shouldBe HttpRequest(
        method = HttpMethods.GET,
        uri = s"/user?uid=${uid}"
      )
    }

    it("should correctly map the response entity to user") {
      val responseUser =
        User(5, Option(2), Option(3), "First Name", "Last Name")
      val responseJson = responseUser.toJson.compactPrint

      val response = HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`application/json`, responseJson)
      )

      client.response = response
      val maybeUser = userDao.getUser(5).value.unsafeRunSync()

      maybeUser shouldBe Option(responseUser)
    }

    it("should return None for a 404 response") {
      val stubResponse = HttpResponse(
        status = StatusCodes.NotFound
      )

      client.response = stubResponse
      val maybeUser = userDao.getUser(5).value.unsafeRunSync()

      maybeUser shouldBe None
    }

    it("should fail for an internal server error") {
      val stubResponse = HttpResponse(
        status = StatusCodes.InternalServerError
      )

      client.response = stubResponse
      val errorOrUser =
        userDao.getUser(5).value.attempt.unsafeRunSync()

      errorOrUser.isLeft shouldBe true
      errorOrUser.left.get.getMessage() should startWith(
        "Unexpected response from user-service"
      )
    }
  }

}

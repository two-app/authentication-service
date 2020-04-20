package user

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import cats.effect.IO
import response.ErrorResponse.ClientError
import response.ErrorResponse.NotFoundError

class UserServiceTest extends AnyFunSpec with Matchers with BeforeAndAfterEach {

  var stubUserDao: StubUserServiceDao[IO] = _
  var userService: UserService[IO] = _
  override def beforeEach(): Unit = {
    stubUserDao = new StubUserServiceDao()
    userService = new UserServiceImpl(stubUserDao)
  }

  describe("getUser by email") {
    it("should return a client error for a malformed email") {
      val errorOrUser = userService.getUser("bademail").value.unsafeRunSync()

      errorOrUser shouldBe Left(ClientError("Invalid email."))
    }

    it("should return a not found error if the user does not exist") {
      stubUserDao.getUserResponse = None

      val errorOrUser =
        userService.getUser("user@two.com").value.unsafeRunSync()

      errorOrUser shouldBe Left(NotFoundError("User does not exist."))
    }

    it("should return the user for a valid email") {
      val user = User(1, Option(2), Option(3), "First", "Last")
      stubUserDao.getUserResponse = Option(user)

      val errorOrUser =
        userService.getUser("user@two.com").value.unsafeRunSync()

      errorOrUser shouldBe Right(user)
    }
  }

}

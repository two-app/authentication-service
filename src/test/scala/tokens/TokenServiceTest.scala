package tokens

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import user.StubUserServiceDao
import cats.effect.IO
import user.UserService
import user.UserServiceImpl
import user.UserTestArbitraries
import user.User
import response.ErrorResponse.AuthorizationError

class TokenServiceTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach
    with UserTestArbitraries {

  var stubUserDao: StubUserServiceDao[IO] = _
  var tokenService: TokenService[IO] = _

  override def beforeEach(): Unit = {
    stubUserDao = new StubUserServiceDao()
    tokenService = new TokenServiceImpl(
      new UserServiceImpl(stubUserDao)
    )
  }

  describe("refreshAccessToken") {
    it("should fail for an invalid token") {
      val errorOrToken =
        tokenService.refreshAccessToken("bla").value.unsafeRunSync()

      errorOrToken shouldBe Left(AuthorizationError("Invalid refresh token."))
    }

    it("should return a new access token for a valid user") {
      val user: User = arbitraryUser()
      stubUserDao.getUserResponse = Option(user)
      val refreshToken: String = RefreshToken.from(5)

      val errorOrToken =
        tokenService.refreshAccessToken(refreshToken).value.unsafeRunSync()

      errorOrToken.isRight shouldBe true
    }
  }
}

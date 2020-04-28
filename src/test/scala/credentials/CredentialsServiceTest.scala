package credentials

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import db.DatabaseTestMixin
import cats.effect.IO
import config.TestServices
import response.ErrorResponse
import response.ErrorResponse.InternalError
import response.ErrorResponse.ClientError
import tokens.Tokens
import user.UserServiceImpl
import user.StubUserServiceDao
import user.UserTestArbitraries
import user.User

 class CredentialsServiceTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach
    with UserTestArbitraries
    with DatabaseTestMixin {

  val services: TestServices = new TestServices()
  val stubUserDao: StubUserServiceDao[IO] = services.stubUserDao
  val credentialsService: CredentialsService[IO] = services.credentialsService

  override def beforeEach(): Unit = cleanMigrate()

  describe("storeCredentials") {
    it("should return new tokens for valid credentials") {
      val creds = EncodedCredentials(UserCredentials(1, "password"))

      val maybeTokens: Either[ErrorResponse, Tokens] =
        credentialsService.storeCredentials(creds).value.unsafeRunSync()

      maybeTokens.isRight shouldBe true
    }

    it("should return an internal server error response for duplicate uids") {
      val creds = EncodedCredentials(UserCredentials(1, "password"))

      credentialsService.storeCredentials(creds).value.unsafeRunSync()
      val maybeTokens: Either[ErrorResponse, Tokens] =
        credentialsService.storeCredentials(creds).value.unsafeRunSync()

      maybeTokens shouldBe Left(InternalError())
    }
  }

  describe("loginWithCredentials") {
    it("should return new tokens for a successful login") {
      val user: User = arbitraryUser()
      val creds = UserCredentials(user.uid, "testPassword")
      val encodedCreds = EncodedCredentials(creds)
      stubUserDao.getUserResponse = Option(user)

      credentialsService.storeCredentials(encodedCreds).value.unsafeRunSync()

      val errorOrTokens = credentialsService.loginWithCredentials(
        LoginCredentials("user@two.com", creds.password)
      ).value.unsafeRunSync()

      errorOrTokens.isRight shouldBe true
    }

    it("should fail with an invalid email") {
      val errorOrTokens = credentialsService.loginWithCredentials(
        LoginCredentials("bla", "rawPassword")
      ).value.unsafeRunSync()

      errorOrTokens shouldBe Left(ClientError("Invalid email."))
    }

    it("should return an internal server error if the user exists but credentials do not") {
      val user: User = arbitraryUser()
      stubUserDao.getUserResponse = Option(user)

      val errorOrTokens = credentialsService.loginWithCredentials(
        LoginCredentials("user@two.com", "testPassword")
      ).value.unsafeRunSync()

      errorOrTokens shouldBe Left(InternalError(s"Failed to find credentials for user with UID ${user.uid}"))
    }

    it("should fail with an incorrect password") {
      val user: User = arbitraryUser()
      val creds = UserCredentials(user.uid, "testPassword")
      val encodedCreds = EncodedCredentials(creds)
      stubUserDao.getUserResponse = Option(user)

      credentialsService.storeCredentials(encodedCreds).value.unsafeRunSync()

      val errorOrTokens = credentialsService.loginWithCredentials(
        LoginCredentials("user@two.com", creds.password + 'z')
      ).value.unsafeRunSync()

      errorOrTokens shouldBe Left(ClientError("Invalid email/password combination."))
    }
  }

}

package credentials

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import db.FlywayHelper
import cats.effect.IO
import config.MasterRoute
import response.ErrorResponse
import response.ErrorResponse.InternalError
import tokens.Tokens
import user.UserServiceImpl
import user.StubUserServiceDao

class CredentialsServiceTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach {

  val credentialsService: CredentialsService[IO] = new CredentialsServiceImpl(
    new UserServiceImpl(new StubUserServiceDao()),
    MasterRoute.services.credentialsDao
  )

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

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

}

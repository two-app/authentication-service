package credentials

import db.DatabaseError
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse.InternalError
import tokens.Tokens

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

class CredentialsServiceImplTest extends AnyFlatSpec with Matchers {
  def storeCredentialsStub(e: Option[DatabaseError]): CredentialsDao = new CredentialsDao {
    override def storeCredentials(credentials: EncodedCredentials): Future[Option[DatabaseError]] = Future(e)

    override def getCredentials(uid: Int): Future[Option[CredentialsRecord]] = null
  }

  val credentials: EncodedCredentials = EncodedCredentials(1, "TestPassword")

  "Database Errors" should "be mapped to Internal Response Errors" in {
    val service = new CredentialsServiceImpl(storeCredentialsStub(Option(DatabaseError.Other())))
    service.storeCredentials(credentials).map(maybeError => {
      maybeError.isLeft shouldBe true
      maybeError.left shouldBe InternalError()
    })
  }

  "successful storage" should "result generate tokens" in {
    val service = new CredentialsServiceImpl(storeCredentialsStub(None))
    service.storeCredentials(credentials).map(maybeError => {
      maybeError.isRight shouldBe true
      maybeError.right shouldBe Tokens(credentials.uid, None, None)
    })
  }
}

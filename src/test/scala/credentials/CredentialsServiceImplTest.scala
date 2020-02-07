package credentials

import db.DatabaseError
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse.InternalError

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
      maybeError.isDefined shouldBe true
      maybeError.get shouldBe InternalError()
    })
  }

  "successful storage" should "result in no errors" in {
    val service = new CredentialsServiceImpl(storeCredentialsStub(None))
    service.storeCredentials(credentials).map(maybeError => {
      maybeError.isEmpty shouldBe true
    })
  }
}

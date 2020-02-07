package credentials

import db.FlywayHelper
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.{global => ec}

class QuillCredentialsDaoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  val credentialsDao: CredentialsDao = new QuillCredentialsDao

  override def beforeEach(): Unit = {
    val flyway = FlywayHelper.getFlyway
    flyway.clean()
    flyway.migrate()
  }

  "stored credentials" should "be retrievable" in {
    val credentials = EncodedCredentials(1, "testPassword")
    credentialsDao.storeCredentials(credentials).flatMap(maybeError => {
      maybeError.isEmpty shouldBe true
      credentialsDao.getCredentials(1)
    }).map(maybeCredentials => {
      maybeCredentials.isEmpty shouldBe false
      maybeCredentials.get.password shouldBe credentials.encodedPassword
    })
  }

}

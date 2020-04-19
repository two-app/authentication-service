package credentials

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import cats.effect.IO
import db.FlywayHelper
import config.MasterRoute

class CredentialsDaoTest
    extends AsyncFunSpec
    with Matchers
    with BeforeAndAfterEach {

  val credentialsDao: CredentialsDao[IO] = MasterRoute.services.credentialsDao

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  describe("storeCredentials") {
    it("should store a new uid password pair") {
      val creds = EncodedCredentials(UserCredentials(1, "password"))

      val affectedRows = credentialsDao.storeCredentials(creds).unsafeRunSync()

      affectedRows shouldBe 1
    }

    it("should not overwrite existing uids") {
      val creds = EncodedCredentials(UserCredentials(1, "password"))

      credentialsDao.storeCredentials(creds).unsafeRunSync()
      val affectedRows = credentialsDao.storeCredentials(creds).unsafeRunSync()

      affectedRows shouldBe 0
    }

    it("should not overwrite existing uids with different passwords") {
      val credsA = EncodedCredentials(UserCredentials(1, "password"))
      val credsB = EncodedCredentials(UserCredentials(1, "another"))

      val affectedRowsA =
        credentialsDao.storeCredentials(credsA).unsafeRunSync()
      val affectedRowsB =
        credentialsDao.storeCredentials(credsB).unsafeRunSync()

      affectedRowsA shouldBe 1
      affectedRowsB shouldBe 0
    }
  }

  describe("getCredentials") {
    it("should return the encoded credentials") {
      val uid = 1
      val creds = EncodedCredentials(UserCredentials(uid, "password"))

      credentialsDao.storeCredentials(creds).unsafeRunSync()
      val maybeCreds = credentialsDao.getCredentials(uid).value.unsafeRunSync()

      maybeCreds shouldBe Option(creds)
    }

    it("should return None for a non existent uid") {
      val maybeCreds = credentialsDao.getCredentials(1).value.unsafeRunSync()
      
      maybeCreds shouldBe None
    }
  }

}

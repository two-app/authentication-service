package credentials

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import db.FlywayHelper
import cats.effect.IO
import config.MasterRoute

class CredentialsServiceTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach {

  val credentialsService: CredentialsService[IO] =
    MasterRoute.services.credentialsService

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

}

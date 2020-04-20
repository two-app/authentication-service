package user

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class EmailValidatorTest extends AnyFunSpec with Matchers {
  it("should fail with an invalid email") {
    EmailValidator.isValid("random") shouldBe false
  }

  it("should succeed for a valid email") {
    EmailValidator.isValid("admin@two.com") shouldBe true
  }
}

package credentials

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.scalatest.funspec.AnyFunSpec

class EncodedCredentialsTest extends AnyFunSpec with Matchers {
  it("should encode raw user credential passwords") {
    val userCredentials = UserCredentials(1, "testPassword")
    EncodedCredentials(userCredentials).encodedPassword should not equal userCredentials.password
  }

  it("should match encoded raw passwords") {
    val userCredentials = UserCredentials(1, "testPassword")
    val encoder = new BCryptPasswordEncoder()

    val encodedCredentials = EncodedCredentials(userCredentials)

    val isPasswordEqual = encoder.matches(
      userCredentials.password,
      encodedCredentials.encodedPassword
    )

    assert(isPasswordEqual)
  }
}

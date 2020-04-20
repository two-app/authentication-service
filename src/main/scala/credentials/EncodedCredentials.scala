package credentials

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

final case class EncodedCredentials(uid: Int, encodedPassword: String)

object EncodedCredentials {
  val encoder: PasswordEncoder = new BCryptPasswordEncoder()

  def apply(userCredentials: UserCredentials): EncodedCredentials = {
    val encodedPassword: String = encoder.encode(userCredentials.password)
    new EncodedCredentials(userCredentials.uid, encodedPassword)
  }
}

object CredentialsMatcher {
  val encoder: PasswordEncoder = new BCryptPasswordEncoder()

  def matches(rawPassword: String, encodedPassword: String): Boolean =
    encoder.matches(rawPassword, encodedPassword)
}
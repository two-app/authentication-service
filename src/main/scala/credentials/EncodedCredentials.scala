package credentials

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

final case class EncodedCredentials(uid: Int, encodedPassword: String)

object EncodedCredentials {
  def apply(uid: Int, rawPassword: String): EncodedCredentials = {
    val encodedPassword = new BCryptPasswordEncoder().encode(rawPassword)
    new EncodedCredentials(uid, encodedPassword)
  }

  def apply(userCredentials: UserCredentials): EncodedCredentials = {
    this.apply(userCredentials.uid, userCredentials.password)
  }
}

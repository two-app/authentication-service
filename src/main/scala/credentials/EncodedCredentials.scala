package credentials

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

final case class EncodedCredentials(uid: Int, encodedPassword: String)

object EncodedCredentials {
  def apply(userCredentials: UserCredentials): EncodedCredentials = {
    val encodedPassword: String = new BCryptPasswordEncoder().encode(userCredentials.password)
    new EncodedCredentials(userCredentials.uid, encodedPassword)
  }
}

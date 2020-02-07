package credentials

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

final case class EncodedCredentials(uid: Int, encodedPassword: String)

object EncodedCredentials {
  def apply(userCredentials: UserCredentials): EncodedCredentials = {
//    val encodedPassword = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(userCredentials.password)
    val encodedPassword = new BCryptPasswordEncoder().encode(userCredentials.password)
    EncodedCredentials(userCredentials.uid, encodedPassword)
  }
}

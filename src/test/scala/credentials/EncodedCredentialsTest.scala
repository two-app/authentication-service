// package credentials

// import org.scalatest.flatspec.AnyFlatSpec
// import org.scalatest.matchers.should.Matchers
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

// class EncodedCredentialsTest extends AnyFlatSpec with Matchers {
//   "encoded password" should "not equal the raw password" in {
//     EncodedCredentials(1, "testPassword").encodedPassword should not equal "testPassword"
//   }

//   "encoded raw passwords" should "match previously encoded passwords" in {
//     val encoder = new BCryptPasswordEncoder()

//     val encodedCredentials = EncodedCredentials(1, "testPassword")

//     val isPasswordEqual = encoder.matches("testPassword", encodedCredentials.encodedPassword)

//     assert(isPasswordEqual)
//   }
// }

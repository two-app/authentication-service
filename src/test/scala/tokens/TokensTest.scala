package tokens

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtOptions}

class TokensTest extends AnyFlatSpec with Matchers {

  "with uid, cid, and pid it" should "generate a refresh + access token pair" in {
    val tokens = Tokens(1, Option(2), Option(3))

    decode(tokens.refreshToken) shouldBe """{"uid": 1, "role": "REFRESH"}"""
    decode(tokens.accessToken) shouldBe """{"uid": 1, "pid": 2, "cid": 3, "role": "ACCESS"}"""
  }

  "with just uid it" should "generate a refresh + connect token pair" in {
    val tokens = Tokens(1, None, None)

    decode(tokens.refreshToken) shouldBe """{"uid": 1, "role": "REFRESH"}"""
    decode(tokens.accessToken) shouldBe """{"uid": 1, "connectCode": "zQp7Wl", "role": "CONNECT"}"""
  }

  def decode(t: String): String = Jwt.decode(t, JwtOptions(signature = false)).get.content

}

package tokens

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtOptions}
import org.scalatest.funspec.AnyFunSpec
import pdi.jwt.JwtClaim
import java.time.Clock
import response.ErrorResponse.AuthorizationError
import response.ErrorResponse.InternalError
import config.Config
import pdi.jwt.JwtAlgorithm

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

  def decode(t: String): String =
    Jwt.decode(t, JwtOptions(signature = false)).get.content

}

class RefreshTokenTest extends AnyFunSpec with Matchers {

  it("should true for validating a created refresh token") {
    val token = RefreshToken.from(5)

    RefreshToken.isValid(token) shouldBe true
  }

  it("should return the users id in a valid decode") {
    val uid = 5
    val token = RefreshToken.from(uid)

    val errorOrUid = RefreshToken.decode(token)

    errorOrUid shouldBe Right(uid)
  }

  it("should return an authorization error for an invalid signed token") {
    val token = Jwt.encode(
      JwtClaim(
        issuer = Option("two"),
        content = s"""{"uid": 5, "role": "REFRESH"}"""
      ).issuedNow(Clock.systemUTC())
    )

    RefreshToken.isValid(token) shouldBe false

    val errorOrUid = RefreshToken.decode(token)

    errorOrUid shouldBe Left(AuthorizationError("Invalid refresh token."))
  }

  it(
    "should return an internal server error for a correctly signed but malformed token"
  ) {
    val claim = JwtClaim(
      issuer = Option("two"),
      content = s"""{"role": "REFRESH"}"""
    ).issuedNow(Clock.systemUTC())

    val token = Jwt.encode(
      claim,
      Config.getProperty("jwt.refresh.secret"),
      JwtAlgorithm.HS256
    )

    RefreshToken.isValid(token) shouldBe true
    val errorOrUid = RefreshToken.decode(token)

    errorOrUid shouldBe Left(InternalError("Failed to decode valid token."))
  }

}

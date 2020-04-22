package tokens

import cats.implicits._
import com.typesafe.config.ConfigFactory
import org.hashids.Hashids
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.RootJsonFormat
import spray.json._
import config.Config
import java.time.Clock
import response.ErrorResponse.AuthorizationError
import response.ErrorResponse.InternalError
import response.ErrorResponse
import scala.util.Try

object AccessToken {

  private val secret: String = Config.getProperty("jwt.access.secret")
  private val salt: String = Config.getProperty("hashids.salt")

  def from(uid: Int, pid: Option[Int], cid: Option[Int]): String = {
    if (pid.isDefined && cid.isDefined) {
      createAccessToken(uid, pid.get, cid.get)
    } else {
      createConnectToken(uid)
    }
  }

  private def createAccessToken(uid: Int, pid: Int, cid: Int): String = {
    val claim = JwtClaim(
      issuer = Option("two"),
      content = s"""{"uid": $uid, "pid": $pid, "cid": $cid, "role": "ACCESS"}"""
    ).issuedNow(Clock.systemUTC())
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }

  private def createConnectToken(uid: Int): String = {
    val connectCode: String = new Hashids(salt, 6).encode(uid)
    val claim = JwtClaim(
      issuer = Option("two"),
      content =
        s"""{"uid": $uid, "connectCode": "$connectCode", "role": "CONNECT"}"""
    ).issuedNow(Clock.systemUTC())
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }
}

object RefreshToken {
  private case class RefreshTokenClaim(uid: Int)
  private implicit val RefreshTokenF: RootJsonFormat[RefreshTokenClaim] =
    jsonFormat1(RefreshTokenClaim)

  private val secret: String = Config.getProperty("jwt.refresh.secret")

  def from(uid: Int): String = {
    val claim = JwtClaim(
      issuer = Option("two"),
      content = s"""{"uid": $uid, "role": "REFRESH"}"""
    ).issuedNow(Clock.systemUTC())
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }

  def isValid(refreshToken: String): Boolean = {
    Jwt.isValid(refreshToken, secret, Seq(JwtAlgorithm.HS256))
  }

  /**
    * @param refreshToken to decode
    * @return the users ID or an authorization error.
    */
  def decode(refreshToken: String): Either[ErrorResponse, Int] = {
    Jwt.decode(refreshToken, secret, Seq(JwtAlgorithm.HS256))
      .toEither
      .leftMap(_ => AuthorizationError("Invalid refresh token."))
      .map(claim => claim.content)
      .map(content => Try(content.parseJson.convertTo[RefreshTokenClaim]))
      .flatMap(attemptedContent => {
        attemptedContent.toEither
          .leftMap(_ => InternalError("Failed to decode valid token."))
      })
      .map(_.uid)
  }

}

case class Tokens(accessToken: String, refreshToken: String)

object Tokens {
  implicit val TokensFormat: RootJsonFormat[Tokens] = jsonFormat2(Tokens.apply)

  def apply(uid: Int, pid: Option[Int], cid: Option[Int]): Tokens = {
    Tokens(
      AccessToken.from(uid, pid, cid),
      RefreshToken.from(uid)
    )
  }
}

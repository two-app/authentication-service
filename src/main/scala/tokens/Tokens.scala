package tokens

import com.typesafe.config.ConfigFactory
import org.hashids.Hashids
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

case class Tokens(accessToken: String, refreshToken: String)

object Tokens {
  def apply(uid: Int, pid: Option[Int], cid: Option[Int]): Tokens = {
    val refreshToken = this.createRefreshToken(uid)

    if (pid.isDefined && cid.isDefined) {
      val accessToken = this.createAccessToken(uid, pid.get, cid.get)
      Tokens(accessToken, refreshToken)
    } else {
      val connectToken = this.createConnectToken(uid)
      Tokens(connectToken, refreshToken)
    }
  }

  private def createAccessToken(uid: Int, pid: Int, cid: Int): String = {
    val secret = getConfigProperty("jwt.access.secret")
    val claim = JwtClaim(issuer = Option("two"), content = s"""{"uid": $uid, "pid": $pid, "cid": $cid, "role": "ACCESS"}""")
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }

  private def createConnectToken(uid: Int): String = {
    val secret = getConfigProperty("jwt.access.secret")
    val salt = getConfigProperty("hashids.salt")
    val connectCode: String = new Hashids(salt, 6).encode(uid);
    val claim = JwtClaim(issuer = Option("two"), content = s"""{"uid": $uid, "connectCode": "$connectCode", "role": "CONNECT"}""")
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }

  private def createRefreshToken(uid: Int): String = {
    val secret = getConfigProperty("jwt.refresh.secret")
    val claim = JwtClaim(issuer = Option("two"), content = s"""{"uid": $uid, "role": "REFRESH"}""")
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }

  private def getConfigProperty(k: String): String = ConfigFactory.load().getString(k)
}
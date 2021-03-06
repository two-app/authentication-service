package tokens

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.HttpRequest
import com.typesafe.scalalogging.Logger
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import request.RouteDispatcher
import cats.data.EitherT
import cats.effect.IO
import response.ErrorResponse
import request.UserContext
import response.ErrorResponse.AuthorizationError
import cats.effect.ConcurrentEffect

case class TokensRequest(uid: Int, pid: Option[Int], cid: Option[Int])

object TokensRequest {
  implicit val TokensRequestFormat: RootJsonFormat[TokensRequest] = jsonFormat3(
    TokensRequest.apply
  )
}

class TokensRouteDispatcher[F[_]: ConcurrentEffect](
    tokenService: TokenService[F]
) extends RouteDispatcher {

  implicit val logger: Logger = Logger[TokensRouteDispatcher[F]]
  val route: Route = extractRequest { request =>
    concat(
      path("tokens") {
        post {
          entity(as[TokensRequest]) { entity => handlePostTokens(entity) }
        }
      },
      path("refresh") {
        post {
          handlePostRefresh(request)
        }
      }
    )
  }

  def handlePostTokens(tokensRequest: TokensRequest): Route = {
    logger.info(s"POST /tokens with data ${tokensRequest}")
    complete(
      Tokens(tokensRequest.uid, tokensRequest.pid, tokensRequest.cid)
    )
  }

  def handlePostRefresh(request: HttpRequest): Route = {
    logger.info("POST /refresh")

    val accessTokenEffect = (for {
      refreshToken <- EitherT.fromEither[F](
        UserContext.extractAuthorizationToken(request)
      )
      accessToken <- tokenService.refreshAccessToken(refreshToken)
    } yield accessToken).leftMap[ErrorResponse](_ =>
      AuthorizationError("Failed to authorize user.")
    )

    completeEffectfulEither(accessTokenEffect)
  }

}

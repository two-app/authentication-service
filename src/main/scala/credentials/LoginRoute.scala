package credentials

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import request.RouteDispatcher
import cats.data.EitherT
import response.ErrorResponse
import tokens.Tokens
import cats.effect.ConcurrentEffect
import com.typesafe.scalalogging.Logger

class LoginRouteDispatcher[F[_]: ConcurrentEffect](
    credentialsService: CredentialsService[F]
) extends RouteDispatcher {

  implicit val logger: Logger = Logger[LoginRouteDispatcher[F]]

  override val route: Route = extractRequest { request =>
    path("login") {
      post {
        entity(as[LoginCredentials]) { credentials =>
          handlePostLogin(request, credentials)
        }
      }
    }
  }

  def handlePostLogin(
      request: HttpRequest,
      loginCredentials: LoginCredentials
  ): Route = {
    logger.info(s"POST /login with email ${loginCredentials.email}")
    completeEffectfulEither(
      credentialsService.loginWithCredentials(loginCredentials)
    )
  }

}

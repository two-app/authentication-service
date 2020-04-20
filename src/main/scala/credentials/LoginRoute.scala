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

class LoginRouteDispatcher(credentialsService: CredentialsService[IO])
    extends RouteDispatcher {

  val loginRoute: LoginRoute[IO] = new LoginRoute(credentialsService)

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
    val tokensEffect = loginRoute.login(loginCredentials)
    onSuccess(tokensEffect.value.unsafeToFuture()) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(tokens: Tokens)      => complete(tokens)
    }
  }

}

class LoginRoute[F[_]](credentialsService: CredentialsService[F]) {

  def login(
      loginCredentials: LoginCredentials
  ): EitherT[F, ErrorResponse, Tokens] = {
    credentialsService.loginWithCredentials(loginCredentials)
  }

}

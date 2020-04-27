package config

import akka.http.scaladsl.server.Route
import cats.effect.ContextShift
import cats.effect.IO
import doobie.util.ExecutionContexts
import credentials.CredentialsRouteDispatcher
import tokens.TokensRouteDispatcher
import request.RouteDispatcher
import credentials.LoginRouteDispatcher
import doobie.util.transactor.Transactor

class MasterRoute(xa: Transactor[IO]) {
  val services: Services[IO] = new Services[IO](xa)

  val credentialsRoute: Route = new CredentialsRouteDispatcher(
    services.credentialsService
  ).route

  val loginRoute: Route = new LoginRouteDispatcher(
    services.credentialsService
  ).route

  val tokensRoute: Route = new TokensRouteDispatcher(
    services.tokenService
  ).route

  val masterRoute: Route = RouteDispatcher.mergeRoutes(
    credentialsRoute,
    tokensRoute,
    loginRoute
  )
}

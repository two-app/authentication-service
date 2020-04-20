package config

import akka.http.scaladsl.server.Route
import cats.effect.ContextShift
import cats.effect.IO
import doobie.util.ExecutionContexts
import credentials.CredentialsRouteDispatcher
import tokens.TokensRouteDispatcher
import request.RouteDispatcher
import credentials.LoginRouteDispatcher

object MasterRoute {
  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)

  val services: Services[IO] = new Services[IO]()

  val credentialsRoute: Route = new CredentialsRouteDispatcher(
    services.credentialsService
  ).route

  val loginRoute: Route = new LoginRouteDispatcher(
    services.credentialsService
  ).route

  val tokensRoute: Route = new TokensRouteDispatcher().route

  val masterRoute: Route = RouteDispatcher.mergeRoutes(
    credentialsRoute,
    tokensRoute,
    loginRoute
  )
}

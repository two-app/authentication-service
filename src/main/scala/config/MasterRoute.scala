package config

import akka.http.scaladsl.server.Route
import tokens.TokensRoute
import cats.effect.ContextShift
import cats.effect.IO
import doobie.util.ExecutionContexts
import credentials.{CredentialsRouteDispatcher}

object MasterRoute {
  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)
  val services: Services[IO] = new Services[IO]()

  val credentialsRoute: Route = new CredentialsRouteDispatcher(services.credentialsService).route
  lazy val tokensRoute: Route = new TokensRoute().route
}

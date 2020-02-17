package config

import akka.http.scaladsl.server.Route
import credentials.{CredentialsDao, CredentialsRoute, CredentialsService, CredentialsServiceImpl, QuillCredentialsDao}
import tokens.TokensRoute

object MasterRoute {
  lazy val credentialsRoute: Route = new CredentialsRoute(credentialsService).route
  lazy val tokensRoute: Route = new TokensRoute().route

  lazy val credentialsService: CredentialsService = new CredentialsServiceImpl(credentialsDao)
  lazy val credentialsDao: CredentialsDao = new QuillCredentialsDao()
}

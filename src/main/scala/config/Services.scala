package config

import akka.http.scaladsl.server.Route
import cats.effect.{Async, ConcurrentEffect, Timer}
import credentials._
import doobie.util.transactor.Transactor
import health._
import request.{RouteDispatcher, ServiceClient}
import tokens.{TokenService, TokenServiceImpl, TokensRouteDispatcher}
import user._

class Services[F[_] : Async : Timer : ConcurrentEffect](xa: Transactor[F]) {
  val userServiceClient: ServiceClient[F] = new UserServiceClient()

  val userDao: UserDao[F] = new UserServiceDao(userServiceClient)
  val credentialsDao: CredentialsDao[F] = new DoobieCredentialsDao(xa)
  val healthDao: HealthDao[F] = new DoobieHealthDao(xa)

  val userService: UserService[F] = new UserServiceImpl(userDao)
  val credentialsService: CredentialsService[F] = new CredentialsServiceImpl(
    userService,
    credentialsDao
  )
  val tokenService: TokenService[F] = new TokenServiceImpl(
    userService
  )
  val healthService: HealthService[F] = new HealthServiceImpl(healthDao)

  val credentialsRouteDispatcher: CredentialsRouteDispatcher[F] =
    new CredentialsRouteDispatcher(
      credentialsService
    )
  val loginRouteDispatcher: LoginRouteDispatcher[F] = new LoginRouteDispatcher(
    credentialsService
  )
  val tokensRouteDispatcher: TokensRouteDispatcher[F] = new TokensRouteDispatcher(
    tokenService
  )
  val healthRouteDispatcher: HealthRouteDispatcher[F] = new HealthRouteDispatcher(
    healthService
  )

  val masterRoute: Route = RouteDispatcher.mergeDispatchers(
    credentialsRouteDispatcher,
    loginRouteDispatcher,
    tokensRouteDispatcher,
    healthRouteDispatcher
  )
}

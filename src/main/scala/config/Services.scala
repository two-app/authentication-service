package config

import cats.effect.ContextShift
import cats.effect.Sync
import cats.effect.Async

import doobie.util.transactor.Transactor.Aux
import credentials.{CredentialsDao, DoobieCredentialsDao}
import credentials.{CredentialsService, CredentialsServiceImpl}
import request.ServiceClient
import user.UserServiceClient
import user.UserDao
import user.UserServiceDao
import user.UserService
import user.UserServiceImpl
import tokens.TokenService
import tokens.TokenServiceImpl
import doobie.util.transactor.Transactor
import credentials.CredentialsRouteDispatcher
import cats.effect.ConcurrentEffect
import credentials.LoginRouteDispatcher
import tokens.TokensRouteDispatcher
import akka.http.scaladsl.server.Route
import request.RouteDispatcher
import health.HealthDao
import health.DoobieHealthDao
import health.HealthService
import health.HealthServiceImpl
import health.HealthRouteDispatcher
import cats.effect.Timer

class Services[F[_]: Async: Timer: ConcurrentEffect](xa: Transactor[F]) {
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

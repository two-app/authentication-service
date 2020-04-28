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

class Services[F[_]: Async: ConcurrentEffect](xa: Transactor[F]) {
  val userServiceClient: ServiceClient[F] = new UserServiceClient[F]()

  val userDao: UserDao[F] = new UserServiceDao[F](userServiceClient)
  val credentialsDao: CredentialsDao[F] = new DoobieCredentialsDao[F](xa)

  val userService: UserService[F] = new UserServiceImpl[F](userDao)
  val credentialsService: CredentialsService[F] = new CredentialsServiceImpl[F](
    userService,
    credentialsDao
  )
  val tokenService: TokenService[F] = new TokenServiceImpl[F](
    userService
  )

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

  val masterRoute: Route = RouteDispatcher.mergeDispatchers(
    credentialsRouteDispatcher,
    loginRouteDispatcher,
    tokensRouteDispatcher
  )
}

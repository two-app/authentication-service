package config

import akka.http.scaladsl.server.Route
import cats.effect.IO
import credentials._
import db.DatabaseTestMixin
import request.{RouteDispatcher, ServiceClient}
import tokens.{TokenService, TokenServiceImpl, TokensRouteDispatcher}
import user._

class TestServices extends DatabaseTestMixin {
  val userServiceClient: ServiceClient[IO] = new UserServiceClient()
  val stubUserServiceClient: StubUserServiceClient[IO] =
    new StubUserServiceClient()

  val userDao: UserDao[IO] = new UserServiceDao(stubUserServiceClient)
  val stubUserDao: StubUserServiceDao[IO] = new StubUserServiceDao
  val credentialsDao: CredentialsDao[IO] = new DoobieCredentialsDao(xa)

  val userService: UserService[IO] = new UserServiceImpl(stubUserDao)
  val credentialsService: CredentialsService[IO] = new CredentialsServiceImpl(
    userService,
    credentialsDao
  )
  val tokenService: TokenService[IO] = new TokenServiceImpl(userService)

  val credentialsRouteDispatcher: CredentialsRouteDispatcher[IO] =
    new CredentialsRouteDispatcher(
      credentialsService
    )
  val loginRouteDispatcher: LoginRouteDispatcher[IO] = new LoginRouteDispatcher(
    credentialsService
  )
  val tokensRouteDispatcher: TokensRouteDispatcher[IO] =
    new TokensRouteDispatcher(tokenService)

  val masterRoute: Route = RouteDispatcher.mergeDispatchers(
    credentialsRouteDispatcher,
    loginRouteDispatcher,
    tokensRouteDispatcher
  )
}

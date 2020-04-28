package config

import db.DatabaseTestMixin
import cats.effect.IO
import credentials.CredentialsDao
import credentials.DoobieCredentialsDao
import user.UserService
import user.UserServiceImpl
import user.UserDao
import user.UserServiceDao
import request.ServiceClient
import user.StubUserServiceClient
import user.UserServiceClient
import credentials.CredentialsService
import credentials.CredentialsServiceImpl
import tokens.TokenService
import tokens.TokenServiceImpl
import credentials.CredentialsRouteDispatcher
import credentials.LoginRouteDispatcher
import tokens.TokensRouteDispatcher
import akka.http.scaladsl.server.Route
import request.RouteDispatcher
import user.StubUserServiceDao

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

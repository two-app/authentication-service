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

class Services[F[_]: Async](xa: Transactor[F]) {
  val userServiceClient: ServiceClient[F] = new UserServiceClient[F]()
  val userDao: UserDao[F] = new UserServiceDao[F](userServiceClient)
  val userService: UserService[F] = new UserServiceImpl[F](userDao)

  val credentialsDao: CredentialsDao[F] = new DoobieCredentialsDao[F](xa)
  val credentialsService: CredentialsService[F] = new CredentialsServiceImpl[F](
    userService,
    credentialsDao
  )

  val tokenService: TokenService[F] = new TokenServiceImpl[F](
    userService
  )
}

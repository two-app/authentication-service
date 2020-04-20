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

class Services[F[_]: Sync: Async: ContextShift] {

  val xa: Aux[F, Unit] = db.transactor[F]()

  val credentialsDao: CredentialsDao[F] = new DoobieCredentialsDao[F](xa)
  val credentialsService: CredentialsService[F] = new CredentialsServiceImpl[F](credentialsDao)

  val userServiceClient: ServiceClient[F] = new UserServiceClient[F]()
  val userDao: UserDao[F] = new UserServiceDao[F](userServiceClient)
}

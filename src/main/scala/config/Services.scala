package config

import cats.effect.ContextShift
import cats.effect.Sync
import cats.effect.Async

import doobie.util.transactor.Transactor.Aux
import credentials.{CredentialsDao, DoobieCredentialsDao}
import credentials.{CredentialsService, CredentialsServiceImpl}

class Services[F[_]: Sync: Async: ContextShift] {

  val xa: Aux[F, Unit] = db.transactor[F]()

  val credentialsDao: CredentialsDao[F] = new DoobieCredentialsDao[F](xa)
  val credentialsService: CredentialsService[F] = new CredentialsServiceImpl[F](credentialsDao)
}

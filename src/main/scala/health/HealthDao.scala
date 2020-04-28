package health

import cats.data.EitherT
import cats.effect.Bracket
import doobie.util.transactor.Transactor
import doobie.implicits._
import java.sql.SQLException
import com.typesafe.scalalogging.Logger

trait HealthDao[F[_]] {
  def performSimpleStatement(): EitherT[F, SQLException, Int]
}

class DoobieHealthDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends HealthDao[F] {

  val logger: Logger = Logger[DoobieHealthDao[F]]

  override def performSimpleStatement(): EitherT[F, SQLException, Int] = {
    EitherT(
      sql"SELECT 1".query[Int].unique.attemptSql.transact(xa)
    )
  }

}

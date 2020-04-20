package request

import cats.effect.Async
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure

object HttpUtils {
  def futureToF[A, F[_]: Async](future: Future[A]): F[A] = {
    Async[F].async { cb =>
      future.onComplete {
        case Success(value)     => cb(Right(value))
        case Failure(exception) => cb(Left(exception))
      }
    }
  }
}


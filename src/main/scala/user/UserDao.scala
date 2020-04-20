package user

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import config.Config
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import spray.json._
import com.typesafe.scalalogging.Logger
import akka.http.scaladsl.model.HttpMethod
import request.ServiceClient
import akka.http.scaladsl.model.HttpEntity

trait UserDao[F[_]] {
  def getUser(email: String): OptionT[F, User]
}

class UserServiceDao[F[_]: Async](client: ServiceClient[F]) extends UserDao[F] {

  val logger: Logger = Logger(classOf[UserServiceDao[F]])
  override def getUser(email: String): OptionT[F, User] = {
    logger.info(s"Retrieving user from user-service with email ${email}")

    val req = HttpRequest(
      method = HttpMethods.GET,
      uri = s"/user?email=${email}"
    )

    OptionT(
      client.perform(req).flatMap(handleResponse)
    ).flatMap(entity => OptionT.liftF(client.unmarshal[User](entity)))
  }

  private def handleResponse(res: HttpResponse): F[Option[ResponseEntity]] =
    res.status match {
      case StatusCodes.OK =>
        res.entity.some.pure[F]
      case StatusCodes.NotFound =>
        none[ResponseEntity].pure[F]
      case _ =>
        Async[F].raiseError[Option[ResponseEntity]](
          new Exception(s"Unexpected response from user-service, ${res}")
        )
    }
}

class UserServiceClient[F[_]: Async] extends ServiceClient[F] {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materialise: ActorMaterializer = ActorMaterializer()

  val location: String = Config.getProperty("service.user.location")

  override def perform(request: HttpRequest): F[HttpResponse] = {
    val newUri = location + request.getUri().toString()
    val newReq = request.copy(uri = newUri)

    HttpUtils.futureToF(
      Http().singleRequest(newReq)
    )
  }

  def unmarshal[A: FromEntityUnmarshaller](response: ResponseEntity): F[A] = {
    HttpUtils.futureToF(Unmarshal(response).to[A])
  }

}

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

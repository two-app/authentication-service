package user

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import com.typesafe.scalalogging.Logger
import config.Config
import config.RootActorSystem._
import request.{HttpUtils, ServiceClient}

trait UserDao[F[_]] {
  def getUser(email: String): OptionT[F, User]

  def getUser(uid: Int): OptionT[F, User]
}

class UserServiceDao[F[_] : Async](client: ServiceClient[F]) extends UserDao[F] {

  val logger: Logger = Logger(classOf[UserServiceDao[F]])

  override def getUser(email: String): OptionT[F, User] = {
    logger.info(s"Retrieving user from user-service with email $email")

    val req = HttpRequest(
      method = HttpMethods.GET,
      uri = s"/user?email=$email"
    )

    OptionT(
      client.perform(req).flatMap(handleResponse)
    ).flatMap(entity => OptionT.liftF(client.unmarshal[User](entity)))
  }

  override def getUser(uid: Int): OptionT[F, User] = {
    logger.info(s"Retrieving user from user-service with UID $uid")

    val req = HttpRequest(
      method = HttpMethods.GET,
      uri = s"/user?uid=$uid"
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
          new Exception(s"Unexpected response from user-service, $res")
        )
    }
}

class UserServiceClient[F[_] : Async] extends ServiceClient[F] {

  val logger: Logger = Logger[UserServiceClient[F]]
  val location: String = Config.getProperty("service.user.location")

  override def perform(request: HttpRequest): F[HttpResponse] = {
    val newUri = location + request.getUri().toString
    val newReq = request.copy(uri = newUri)
    logger.info(f"Performing request $newReq")

    HttpUtils.futureToF(
      Http().singleRequest(newReq)
    )
  }

  override def unmarshal[A: FromEntityUnmarshaller](response: ResponseEntity): F[A] = {
    logger.info(f"Unmarshalling response $response")
    HttpUtils.futureToF(Unmarshal(response).to[A])
  }

}
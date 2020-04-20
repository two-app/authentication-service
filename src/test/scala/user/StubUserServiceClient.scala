package user

import request.ServiceClient
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import cats.effect.Sync
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.model.StatusCodes
import cats.effect.Async

/**
  * Stubs the response and perform definitions of the
  * UserServiceClient. Other definitions are delegated
  * to the implementation.
  */
class StubUserServiceClient[F[_]: Sync: Async] extends ServiceClient[F] {

  var lastRequest: HttpRequest = _
  var response: HttpResponse = HttpResponse(status = StatusCodes.NotFound)
  val impl: ServiceClient[F] = new UserServiceClient[F]()

  override def location: String = "http://test.location"

  override def perform(request: HttpRequest): F[HttpResponse] = {
    lastRequest = request
    Sync[F].pure(response)
  }

  override def unmarshal[A: FromEntityUnmarshaller](
      response: ResponseEntity
  ): F[A] = impl.unmarshal[A](response)

}

package request

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller

trait ServiceClient[F[_]] {
  def location: String
  def perform(request: HttpRequest): F[HttpResponse]
  def unmarshal[A: FromEntityUnmarshaller](response: ResponseEntity): F[A]
}

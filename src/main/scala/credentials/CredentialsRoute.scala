package credentials

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.Logger
import response.ErrorResponse
import response.ErrorResponse.ClientError
import request.RouteDispatcher
import tokens.Tokens
import cats.data.EitherT
import cats.effect.ConcurrentEffect
import cats.implicits._

class CredentialsRouteDispatcher[F[_] : ConcurrentEffect](credentialsService: CredentialsService[F])
    extends RouteDispatcher {

  implicit val logger: Logger = Logger[CredentialsRouteDispatcher[F]]

  override val route: Route = extractRequest { request =>
    path("credentials") {
      post {
        entity(as[Either[ModelValidationError, UserCredentials]]) {
          maybeUserCredentials =>
            handlePostCredentials(
              request,
              maybeUserCredentials
            )
        }
      }
    }
  }

  def handlePostCredentials(
      request: HttpRequest,
      maybeCredentials: Either[ModelValidationError, UserCredentials]
  ): Route = {
    logger.info("POST /credentials")
    val tokensEffect = for {
      credentials <- maybeCredentials.toEitherT[F].leftMap(e => ClientError(e.reason))
      tokens <- credentialsService.storeCredentials(EncodedCredentials(credentials))
    } yield tokens

    completeEffectfulEither(tokensEffect)
  }

}
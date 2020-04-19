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

class CredentialsRouteDispatcher(credentialsService: CredentialsService[IO])
    extends RouteDispatcher {

      val logger: Logger = Logger(classOf[CredentialsRouteDispatcher])

  val credentialsRoute: CredentialsRoute[IO] = new CredentialsRoute(
    credentialsService
  )

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
    val tokensEffect: EitherT[IO, ErrorResponse, Tokens] = EitherT
      .fromEither[IO](maybeCredentials)
      .leftMap[ErrorResponse](e => ClientError(e.reason))
      .flatMap(credentialsRoute.storeCredentials)

    onSuccess(tokensEffect.value.unsafeToFuture()) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(tokens: Tokens) => complete(tokens)
    }
  }

}

class CredentialsRoute[F[_]](credentialsService: CredentialsService[F]) {

  def storeCredentials(
      userCredentials: UserCredentials
  ): EitherT[F, ErrorResponse, Tokens] = {
    credentialsService.storeCredentials(EncodedCredentials(userCredentials))
  }

}

// class CredentialsRoute(credentialsService: CredentialsService) {
//   val logger: Logger = Logger(classOf[CredentialsRoute])
//   val route: Route = path("credentials") {
//     post {
//       extractRequest { request => postCredentials(request) }
//     }
//   }

//   def postCredentials(request: HttpRequest): Route = {
//     logger.info("POST /credentials")
//     entity(as[Either[ModelValidationError, UserCredentials]]) {
//       case Left(e) =>
//         val clientError: ErrorResponse = ClientError(e.reason)
//         complete(clientError.status, clientError)
//       case Right(userCredentials) => storeCredentials(userCredentials)
//     }
//   }

//   def storeCredentials(userCredentials: UserCredentials): Route = {
//     val encodedCredentials = EncodedCredentials(userCredentials)
//     onSuccess(credentialsService.storeCredentials(encodedCredentials)) {
//       case Left(e)       => complete(e.status, e)
//       case Right(tokens) => complete(tokens)
//     }
//   }
// }

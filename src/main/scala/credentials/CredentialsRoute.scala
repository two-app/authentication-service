package credentials

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import response.ErrorResponse
import response.ErrorResponse.ClientError
import tokens.Tokens._

class CredentialsRoute(credentialsService: CredentialsService) {
  val logger: Logger = Logger(classOf[CredentialsRoute])
  val route: Route = path("credentials") {
    post {
      extractRequest { request =>
        postCredentials(request)
      }
    }
  }

  def postCredentials(request: HttpRequest): Route = {
    logger.info("POST /credentials")
    entity(as[Either[ModelValidationError, UserCredentials]]) {
      case Left(e) =>
        val clientError: ErrorResponse = ClientError(e.reason)
        complete(clientError.status, clientError)
      case Right(userCredentials) => storeCredentials(userCredentials)
    }
  }

  def storeCredentials(userCredentials: UserCredentials): Route = {
    val encodedCredentials = EncodedCredentials(userCredentials)
    onSuccess(credentialsService.storeCredentials(encodedCredentials)) {
      case Left(e) => complete(e.status, e)
      case Right(tokens) => complete(tokens)
    }
  }
}

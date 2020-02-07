package credentials

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import response.ErrorResponse
import response.ErrorResponse.ClientError

class CredentialsRoute {
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
    print(userCredentials)
    complete("lol")
  }
}

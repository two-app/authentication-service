package tokens

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import request.RouteDispatcher

case class TokensRequest(uid: Int, pid: Option[Int], cid: Option[Int])

object TokensRequest {
  implicit val TokensRequestFormat: RootJsonFormat[TokensRequest] = jsonFormat3(
    TokensRequest.apply
  )
}

class TokensRouteDispatcher extends RouteDispatcher {

  val logger: Logger = Logger(classOf[TokensRouteDispatcher])
  val route: Route = extractRequest { request =>
    path("tokens") {
      post {
        entity(as[TokensRequest]) { entity => handlePostTokens(entity) }
      }
    }
  }

  def handlePostTokens(tokensRequest: TokensRequest): Route = {
    logger.info(s"POST /tokens with data ${tokensRequest}")
    complete(
      Tokens(tokensRequest.uid, tokensRequest.pid, tokensRequest.cid)
    )
  }

}

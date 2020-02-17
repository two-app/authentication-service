package tokens

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class TokensRequest(uid: Int, pid: Option[Int], cid: Option[Int])

object TokensRequest {
  implicit val TokensRequestFormat: RootJsonFormat[TokensRequest] = jsonFormat3(TokensRequest.apply)
}

class TokensRoute {
  val logger: Logger = Logger(classOf[TokensRoute])

  val route: Route = path("tokens") {
    post {
      entity(as[TokensRequest]) {
        entity => createTokens(entity)
      }
    }
  }

  def createTokens(req: TokensRequest): Route = complete(Tokens(req.uid, req.pid, req.cid))
}

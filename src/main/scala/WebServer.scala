import akka.http.scaladsl.server.{HttpApp, Route}
import config.MasterRoute._

object Server extends HttpApp {
  override protected def routes: Route = credentialsRoute ~ tokensRoute
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8082)
  }
}
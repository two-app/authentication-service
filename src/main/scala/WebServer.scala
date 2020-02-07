import akka.http.scaladsl.server.{HttpApp, Route}
import credentials.CredentialsRoute

object Server extends HttpApp {
  override protected def routes: Route = new CredentialsRoute().route
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8081)
  }
}
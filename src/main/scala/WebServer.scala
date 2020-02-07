import akka.http.scaladsl.server.{HttpApp, Route}
import credentials.{CredentialsRoute, CredentialsServiceImpl, QuillCredentialsDao}

object Server extends HttpApp {
  override protected def routes: Route = new CredentialsRoute(new CredentialsServiceImpl(new QuillCredentialsDao)).route
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8081)
  }
}
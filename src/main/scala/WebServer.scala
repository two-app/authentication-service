import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.{ExitCode, IO, IOApp}
import config.MasterRoute

class Server extends HttpApp {
  override protected def routes: Route = MasterRoute.masterRoute
}

object WebServer extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    new Server().startServer("localhost", 8082)
    IO(ExitCode.Success)
  }
}
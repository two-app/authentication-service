import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.{ExitCode, IO, IOApp}
import config.MasterRoute
import config.Config

class Server extends HttpApp {
  override protected def routes: Route = MasterRoute.masterRoute
}

object WebServer extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val port: Int = Config.load().getInt("server.port")
    new Server().startServer("localhost", port)
    IO(ExitCode.Success)
  }
}
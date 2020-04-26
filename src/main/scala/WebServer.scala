import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.{ExitCode, IO, IOApp}
import config.MasterRoute
import config.Config
import com.typesafe.scalalogging.Logger

class Server extends HttpApp {
  override protected def routes: Route = MasterRoute.masterRoute
}

object WebServer extends IOApp {

  val logger: Logger = Logger("WebServer")

  def run(args: List[String]): IO[ExitCode] = {
    val host: String = Config.load().getString("server.host")
    val port: Int = Config.load().getInt("server.port")

    logger.info(s"Starting server on configured host ${host} and port ${port}.")
    new Server().startServer(host, port)

    logger.info("Server finished blocking. Exiting with success.")
    IO(ExitCode.Success)
  }
}

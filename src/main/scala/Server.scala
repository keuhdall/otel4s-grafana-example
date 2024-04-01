import cats.effect.*
import cats.effect.std.Random
import com.comcast.ip4s.{ipv4, port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

object Server extends IOApp {
  private def app[F[_]: Async: LiftIO]: Resource[F, Server] =
    given LoggerFactory[F] = Slf4jFactory.create[F]
    for {
      given Random[F] <- Resource.eval(Random.scalaUtilRandom[F])
      otel <- OtelJava.autoConfigured[F]()
      serviceName = "otel4s-grafana-example"
      given Meter[F] <- Resource.eval(otel.meterProvider.get(serviceName))
      given Tracer[F] <- Resource.eval(otel.tracerProvider.get(serviceName))
      exampleService <- Resource.eval(ExampleService[F](50, 500, 20))
      route <- Resource.eval(ExampleRoute[F](exampleService))
      server <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(route.orNotFound.traced)
        .build
    } yield server

  override def run(args: List[String]): IO[ExitCode] =
    app[IO].useForever.as(ExitCode.Success)
}

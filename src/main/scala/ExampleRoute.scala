import cats.effect.Async
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.otel4s.metrics.Meter

object ExampleRoute {
  def apply[F[_]: Async: LoggerFactory: Meter](
      exampleService: ExampleService[F]
  ): F[HttpRoutes[F]] = {
    object serverDsl extends Http4sDsl[F]; import serverDsl.*
    val metricsProvider = summon[Meter[F]]
    val logger = LoggerFactory[F].getLogger

    metricsProvider
      .counter("ExampleRoute.count")
      .withDescription("number of times the route /example is called")
      .create
      .map { endpointCounter =>
        HttpRoutes.of[F] { case req @ GET -> Root / "example" =>
          for {
            _ <- logger.info(
              s"received request from ${req.from.fold("<unknown>")(_.toUriString)}"
            )
            _ <- endpointCounter.inc()
            data <- exampleService.getDataFromSomeAPI
            result <- Ok(data)
          } yield result
        }
      }
  }
}

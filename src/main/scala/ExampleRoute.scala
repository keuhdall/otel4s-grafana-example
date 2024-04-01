import cats.effect.Async
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.otel4s.metrics.Meter

object ExampleRoute {
  def apply[F[_]: Async: Meter](exampleService: ExampleService[F]): F[HttpRoutes[F]] = {
    object serverDsl extends Http4sDsl[F]; import serverDsl.*
    val metricsProvider = summon[Meter[F]]

    metricsProvider
      .counter[Long]("ExampleRoute.count")
      .withDescription("number of times the route /example is called")
      .create
      .map { endpointCounter =>
        HttpRoutes.of[F] { case GET -> Root / "example" =>
          for {
            _ <- endpointCounter.inc()
            data <- exampleService.getDataFromSomeAPI
            result <- Ok(data)
          } yield result
        }
      }
  }
}

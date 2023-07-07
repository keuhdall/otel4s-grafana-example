import cats.effect.Async
import cats.effect.std.Random
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import io.circe.derivation.{Configuration, ConfiguredCodec}
import org.typelevel.otel4s.{Attribute, AttributeKey}
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

given Configuration = Configuration.default.withSnakeCaseMemberNames
case class ApiData(result: String) derives ConfiguredCodec

trait ExampleService[F[_]] {
  def getDataFromSomeAPI: F[ApiData]
}

object ExampleService {
  def apply[F[_]: Async: Tracer: Meter: Random](
      minLatency: Int,
      maxLatency: Int,
      bananaPercentage: Int
  ): ExampleService[F] =
    new ExampleService[F] {
      private val metricsProvider = summon[Meter[F]]
      private val remoteApiFruitCount = metricsProvider
        .counter("RemoteApi.fruit.count")
        .withDescription("Number of fruits returned by the API.")
        .create

      private val spanBuilder = Tracer[F].spanBuilder("remoteAPI.com/fruit").build

      override def getDataFromSomeAPI: F[ApiData] = for {
        latency <- Random[F].betweenInt(minLatency, maxLatency)
        isBanana <- Random[F].betweenInt(0, 100).map(_ <= bananaPercentage)
        duration = FiniteDuration(latency, TimeUnit.MILLISECONDS)
        fruit <- spanBuilder.surround(
          Async[F].sleep(duration) *> Async[F].pure(
            if isBanana then "banana" else "apple"
          )
        )
        _ <- remoteApiFruitCount.flatMap(
          _.inc(Attribute(AttributeKey.string("fruit"), fruit))
        )
      } yield ApiData(s"Api returned a $fruit !")
    }
}

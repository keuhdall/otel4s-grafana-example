import cats.MonadThrow
import cats.data.Kleisli
import cats.implicits.*
import org.http4s.{Header, HttpApp, Request, Response}
import org.typelevel.ci.CIString
import org.typelevel.otel4s.trace.{SpanKind, Tracer}
import org.typelevel.otel4s.{Attribute, AttributeKey}

// Implementation based upon Ivan Kurchenko's article:
// https://ivan-kurchenko.medium.com/telemetry-with-scala-part-3-otel4s-c5c150303164

extension [F[_]: MonadThrow: Tracer](service: HttpApp[F]) {
  def traced: Kleisli[F, Request[F], Response[F]] = Kleisli { req =>
    Tracer[F]
      .spanBuilder(s"${req.method.name} ${req.uri.path.toString}")
      .addAttribute(
        Attribute(AttributeKey.string("http.request.method"), req.method.name)
      )
      .addAttribute(
        Attribute(
          AttributeKey.string("http.client_ip"),
          req.remoteAddr.fold("<unknown>")(_.toString)
        )
      )
      .addAttribute(
        Attribute(
          AttributeKey.long("http.request.body.size"),
          req.contentLength.getOrElse(0L)
        )
      )
      .withSpanKind(SpanKind.Server)
      .build
      .use { span =>
        for {
          response <- service(req)
          _ <- span.addAttribute(
            Attribute(AttributeKey.long("http.status_code"), response.status.code)
          )
          _ <- span.addAttribute(
            Attribute(
              AttributeKey.long("http.response.body.size"),
              response.contentLength.getOrElse(0L)
            )
          )
        } yield {
          val traceIdHeader = Header.Raw(CIString("traceId"), span.context.traceIdHex)
          response.putHeaders(traceIdHeader)
        }
      }
  }
}

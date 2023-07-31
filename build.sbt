ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "otel4s-grafana-example",
    dockerBaseImage := "openjdk:11",
    dockerExposedPorts ++= Seq(8080, 5000),
    libraryDependencies ++= commonDeps ++ circeDeps ++ http4sDeps ++ logDeps ++ otelDeps,
    scalacOptions ++= Seq(
      "-Wunused:all",
      "-Wvalue-discard",
      "-language:implicitConversions",
      "-source:future",
      "-feature",
      "-deprecation"
    ),
    Universal / javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      s"-Dotel.service.name=${name.value}",
      "-jvm-debug 5000"
    ),
    scalafmtOnCompile := true
  )
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(ScalafmtPlugin)

lazy val commonDeps = Seq(deps.cats, deps.catsEffect)
lazy val circeDeps = Seq(deps.circe, deps.circeGeneric)
lazy val http4sDeps = Seq(deps.http4sServer, deps.http4sDsl, deps.http4sCirce)
lazy val logDeps = Seq(deps.logback, deps.otelLogbackAppender, deps.log4cats)
lazy val otelDeps = Seq(deps.otel4s, deps.otelExporter, deps.otelSdk)

lazy val deps = new {
  val catsVersion = "2.9.0"
  val catsEffectVersion = "3.5.0"
  val circeVersion = "0.14.5"
  val http4sVersion = "1.0.0-M39"
  val otel4sVersion = "0.2.1"
  val otelVersion = "1.27.0"
  val otelSdkVersion = "1.27.0-alpha"
  val log4catsVersion = "2.5.0"
  val logbackVersion = "1.4.8"

  val cats = "org.typelevel" %% "cats-core" % catsVersion
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion

  val circe = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion

  val http4sServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion

  val log4cats = "org.typelevel" %% "log4cats-slf4j" % log4catsVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

  val otel4s = "org.typelevel" %% "otel4s-java" % otel4sVersion
  val otelExporter =
    "io.opentelemetry" % "opentelemetry-exporter-otlp" % otelVersion % Runtime
  val otelSdk =
    "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % otelSdkVersion % Runtime
  val otelLogbackAppender =
    "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % otelSdkVersion % Runtime
}

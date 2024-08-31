ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.3"

lazy val root = (project in file("."))
  .settings(
    name := "otel4s-grafana-example",
    dockerExposedPorts ++= Seq(8080),
    libraryDependencies ++= commonDeps ++ circeDeps ++ http4sDeps ++ logDeps ++ otelDeps,
    scalacOptions ++= Seq(
      "-Wunused:all",
      "-Wvalue-discard",
      "-language:implicitConversions",
      "-source:future",
      "-feature",
      "-deprecation"
    ),
    Compile / javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      s"-Dotel.service.name=${name.value}"
    ),
    Universal / javaOptions ++= (Compile / javaOptions).value,
    fork := true,
    scalafmtOnCompile := true
  )
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(ScalafmtPlugin)

lazy val commonDeps = Seq(deps.cats, deps.catsEffect)
lazy val circeDeps = Seq(deps.circe, deps.circeGeneric)
lazy val http4sDeps = Seq(deps.http4sServer, deps.http4sDsl, deps.http4sCirce)
lazy val logDeps = Seq(deps.log4cats, deps.logback)
lazy val otelDeps = Seq(deps.otel4s, deps.otelExporter, deps.otelSdk)

lazy val deps = new {
  val catsVersion = "2.12.0"
  val catsEffectVersion = "3.5.4"
  val circeVersion = "0.14.9"
  val http4sVersion = "1.0.0-M41"
  val otel4sVersion = "0.9.0"
  val otelVersion = "1.36.0"
  val slf4jVersion = "2.0.5"
  val log4catsVersion = "2.7.0"
  val logbackVersion = "1.5.7"

  val cats = "org.typelevel" %% "cats-core" % catsVersion
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion

  val circe = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion

  val http4sServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion

  val log4cats = "org.typelevel" %% "log4cats-slf4j" % log4catsVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

  val otel4s = "org.typelevel" %% "otel4s-oteljava" % otel4sVersion
  val otelExporter =
    "io.opentelemetry" % "opentelemetry-exporter-otlp" % otelVersion % Runtime
  val otelSdk =
    "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % otelVersion % Runtime
}

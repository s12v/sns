package me.snov.sns

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import me.snov.sns.api._
import me.snov.sns.utils.Config

import scala.concurrent.ExecutionContext

object Main extends App with Config with HealthCheck {
  implicit val system = ActorSystem("sns")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val logger: LoggingAdapter = Logging(system, getClass)
  implicit val timeout = new Timeout(1000, TimeUnit.MILLISECONDS)

  val homeActor = system.actorOf(HomeApi.props)
  val topicActor = system.actorOf(TopicApi.props)
  val subscribeActor = system.actorOf(SubscribeApi.props)

  val routes: Route = {
      TopicApi.route(topicActor) ~
      SubscribeApi.route(subscribeActor) ~
      healthCheckRoutes ~
      HomeApi.route(homeActor)
  }

  Http().bindAndHandle(
    handler = logRequestResult("akka-http-sns")(routes),
    interface = config.getString("http.interface"),
    port = config.getInt("http.port")
  )
}

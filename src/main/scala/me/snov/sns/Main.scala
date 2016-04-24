package me.snov.sns

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import me.snov.sns.api._
import me.snov.sns.util.Config

import scala.concurrent.ExecutionContext

object Main extends App with Config with HealthCheck {
  implicit val system = ActorSystem("sns")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val logger: LoggingAdapter = Logging(system, getClass)
  implicit val timeout = new Timeout(1000, TimeUnit.MILLISECONDS)

  val homeActor = system.actorOf(HomeActor.props, name = "HomeActor")
  val topicActor = system.actorOf(TopicActor.props, name = "TopicActor")
  val subscribeActor = system.actorOf(SubscribeActor.props, name = "SubscribeActor")
  val publishActor = system.actorOf(PublishActor.props(subscribeActor), name = "PublishActor")

  val routes: Route = {
      TopicApi.route(topicActor) ~
      SubscribeApi.route(subscribeActor) ~
      PublishApi.route(publishActor) ~
      healthCheckRoutes ~
      HomeApi.route(homeActor)
  }

  Http().bindAndHandle(
    handler = logRequestResult("akka-http-sns")(routes),
    interface = config.getString("http.interface"),
    port = config.getInt("http.port")
  )
}

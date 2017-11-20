package me.snov.sns

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import me.snov.sns.actor._
import me.snov.sns.api._
import me.snov.sns.service.FileDbService
import me.snov.sns.util.ToStrict

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Properties

object Main extends App with ToStrict {
  implicit val system = ActorSystem("sns")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val logger: LoggingAdapter = Logging(system, getClass)
  implicit val timeout = new Timeout(1.second)

  val config = ConfigFactory.load()
  val dbService = new FileDbService(Properties.envOrElse("DB_PATH", config.getString("db.path")))

  val dbActor = system.actorOf(DbActor.props(dbService), name = "DbActor")
  val homeActor = system.actorOf(HomeActor.props, name = "HomeActor")
  val subscribeActor = system.actorOf(SubscribeActor.props(dbActor), name = "SubscribeActor")
  val publishActor = system.actorOf(PublishActor.props(subscribeActor), name = "PublishActor")

  val routes: Route =
    toStrict {
      TopicApi.route(subscribeActor) ~
      SubscribeApi.route(subscribeActor) ~
      PublishApi.route(publishActor) ~
      HealthCheckApi.route ~
      HomeApi.route(homeActor)
    }

  logger.info("SNS v{} is starting", getClass.getPackage.getImplementationVersion)

  Http().bindAndHandle(
    handler = logRequestResult("akka-http-sns")(routes),
    interface = Properties.envOrElse("HTTP_INTERFACE", config.getString("http.interface")),
    port = Properties.envOrElse("HTTP_PORT", config.getString("http.port")).toInt
  )
}

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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem("sns")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val logger: LoggingAdapter = Logging(system, getClass)
  implicit val timeout = new Timeout(1.second)

  val config = ConfigFactory.load()

  val dbActor = system.actorOf(DbActor.props, name = "DbActor")
  val homeActor = system.actorOf(HomeActor.props, name = "HomeActor")
  val topicActor = system.actorOf(TopicActor.props(dbActor), name = "TopicActor")
  val subscribeActor = system.actorOf(SubscribeActor.props(dbActor), name = "SubscribeActor")
  val publishActor = system.actorOf(PublishActor.props(subscribeActor), name = "PublishActor")

  val toStrict = mapInnerRoute { innerRoute =>
    extractRequest { req =>
      onSuccess(req.toStrict(1.second)) { strictReq =>
        mapRequest(_ => strictReq) {
          innerRoute
        }
      }
    }
  }

  val routes: Route =
    toStrict {
      TopicApi.route(topicActor) ~
      SubscribeApi.route(subscribeActor) ~
      PublishApi.route(publishActor) ~
      HealthCheckApi.route ~
      HomeApi.route(homeActor)
    }

  Http().bindAndHandle(
    handler = logRequestResult("akka-http-sns")(routes),
    interface = config.getString("http.interface"),
    port = config.getInt("http.port")
  )
}

package me.snov.sns.api

import java.util.UUID

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.http.scaladsl.model.{FormData, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.api.SubscribeActor._
import me.snov.sns.model.Message

import scala.collection.mutable

object SubscribeApi {
  private val arnPattern = """([\w+_:-]{1,512})""".r

  def route(actorRef: ActorRef)(implicit timeout: Timeout): Route = {
    pathSingleSlash {
      entity(as[FormData]) { entity =>
        entity.fields.get("Action") match {
          case Some("Subscribe") =>
            val endpoint = entity.fields.get("Endpoint") 
            val protocol = entity.fields.get("Protocol") 
            val topicArn = entity.fields.get("TopicArn") 
            if (endpoint.isDefined && protocol.isDefined && topicArn.isDefined) {
              complete {
                (actorRef ? CmdSubscribe(endpoint.get, protocol.get, topicArn.get)).mapTo[HttpResponse]
              }
            } else {
              complete(HttpResponse(400, entity = "Endpoint, Protocol, TopicArn are required"))
            }
          case Some("ListSubscriptionsByTopic") =>
            entity.fields.getOrElse("TopicArn", "") match {
              case arnPattern(topicArn) => complete {
                (actorRef ? CmdListByTopic(topicArn)).mapTo[HttpResponse]
              }
              case _ => complete(HttpResponse(400, entity = "Invalid topic ARN"))
            }
          case Some("ListSubscriptions") =>
            complete {
              (actorRef ? CmdList()).mapTo[HttpResponse]
            }
          case default => reject()
        }
      }
    }
  }
}

object SubscribeResponses extends XmlHttpResponse {
  def subscribe(subscriptionArn: String) = {
    response(
      <SubscribeResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <SubscribeResult>
          <SubscriptionArn>
            {subscriptionArn}
          </SubscriptionArn>
        </SubscribeResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </SubscribeResponse>
    )
  }

  def list(subscriptions: Iterable[Subscription]): HttpResponse = {
    response(
      <ListSubscriptionsResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ListSubscriptionsResult>
          <Subscriptions>
            {subscriptions.map(subscription =>
            <member>
              <Owner>
                {subscription.owner}
              </Owner>
              <Protocol>
                {subscription.protocol}
              </Protocol>
              <Endpoint>
                {subscription.endpoint}
              </Endpoint>
              <SubscriptionArn>
                {subscription.subscriptionArn}
              </SubscriptionArn>
              <TopicArn>
                {subscription.topicArn}
              </TopicArn>
            </member>
          )}
          </Subscriptions>
        </ListSubscriptionsResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </ListSubscriptionsResponse>
    )
  }

  def listByTopic(subscriptions: List[Subscription]): HttpResponse = {
    response(
      <ListSubscriptionsByTopicResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ListSubscriptionsByTopicResult>
          <Subscriptions>
            {subscriptions.map(subscription =>
            <member>
              <Owner>
                {subscription.owner}
              </Owner>
              <Protocol>
                {subscription.protocol}
              </Protocol>
              <Endpoint>
                {subscription.endpoint}
              </Endpoint>
              <SubscriptionArn>
                {subscription.subscriptionArn}
              </SubscriptionArn>
              <TopicArn>
                {subscription.topicArn}
              </TopicArn>
            </member>
          )}
          </Subscriptions>
        </ListSubscriptionsByTopicResult>
        <ResponseMetadata>
          <RequestId>b9275252-3774-11df-9540-99d0768312d3</RequestId>
        </ResponseMetadata>
      </ListSubscriptionsByTopicResponse>
    )
  }
}

object SubscribeActor {
  def props = Props[SubscribeActor]

  case class CmdSubscribe(endpoint: String, protocol: String, topicArn: String)

  case class CmdList()

  case class CmdListByTopic(topicArn: String)

  case class CmdFanOut(topicArn: String, message: Message)

  case class Subscription(
                           topicArn: String,
                           subscriptionArn: String,
                           protocol: String,
                           endpoint: String,
                           owner: String
                         ) {
    def this(topicArn: String, protocol: String, endpoint: String) =
      this(topicArn, UUID.randomUUID().toString, protocol, endpoint, "")
  }
}


class SubscribeActor extends Actor with ActorLogging {

  var subscriptions = mutable.HashMap.empty[String, List[Subscription]]

  private def fanOut(topicArn: String, message: Message) = {
    subscriptions.get(topicArn) match {
      case Some(ss: List[Subscription]) =>
        ss.foreach((s: Subscription) => 
          log.info(s.endpoint)
        )
      case None => log.info(s"Not found by $topicArn")
    }
  }
  
  private def subscribe(endpoint: String, protocol: String, topicArn: String): HttpResponse = {
    val subscription = new Subscription(topicArn, protocol, endpoint)
    subscriptions.put(topicArn, subscription :: subscriptions.getOrElse(topicArn, List()))
    SubscribeResponses.subscribe(subscription.subscriptionArn)
  }

  private def listByTopic(topicArn: String): HttpResponse = {
    SubscribeResponses.listByTopic(subscriptions.getOrElse(topicArn, List()))
  }

  private def list(): HttpResponse = {
    SubscribeResponses.list(subscriptions.values.flatten)
  }

  override def receive = {
    case CmdSubscribe(endpoint, protocol, topicArn) => sender ! subscribe(endpoint, protocol, topicArn)
    case CmdListByTopic(topicArn) => sender ! listByTopic(topicArn)
    case CmdList() => sender ! list()
    case CmdFanOut(topicArn, message) => fanOut(topicArn, message)
    case _ => sender ! HttpResponse(500, entity = "Invalid message")
  }
}

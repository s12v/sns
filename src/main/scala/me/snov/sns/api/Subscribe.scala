package me.snov.sns.api

import java.util.UUID

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.http.scaladsl.model.HttpResponse
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
      formField('Action ! "Subscribe") {
        formFields('Endpoint, 'Protocol, 'TopicArn) { (endpoint, protocol, topicArn) =>
          complete {
            (actorRef ? CmdSubscribe(endpoint, protocol, topicArn)).mapTo[HttpResponse]
          }
        } ~
          complete(HttpResponse(400, entity = "Endpoint, Protocol, TopicArn are required"))
      } ~
        formField('Action ! "ListSubscriptionsByTopic") {
          formField('TopicArn) {
            case arnPattern(topicArn) => complete {
              (actorRef ? CmdListByTopic(topicArn)).mapTo[HttpResponse]
            }
            case _ => complete(HttpResponse(400, entity = "Invalid topic ARN"))
          } ~
            complete(HttpResponse(400, entity = "TopicArn is missing"))
        } ~
        formField('Action ! "ListSubscriptions") {
          complete {
            (actorRef ? CmdList()).mapTo[HttpResponse]
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

  case class Subscription(topicArn: String, protocol: String, endpoint: String) {
    val subscriptionArn = UUID.randomUUID().toString
    val owner = ""
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

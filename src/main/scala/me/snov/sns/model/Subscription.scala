package me.snov.sns.model

import java.util.UUID

import akka.actor.ActorRef
import spray.json.DefaultJsonProtocol

case class Subscription(topicArn: String, protocol: String, endpoint: String, actorRef: ActorRef) {
  val subscriptionArn = UUID.randomUUID().toString
  val owner = ""
}

//object SubscriptionConfigurationJsonProtocol extends DefaultJsonProtocol {
//  //  implicit val format = jsonFormat3(SubscriptionConfiguration)
//}

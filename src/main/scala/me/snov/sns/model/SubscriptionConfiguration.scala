package me.snov.sns.model

import spray.json.DefaultJsonProtocol

case class SubscriptionConfiguration(topicArn: String, protocol: String, endpoint: String)

object SubscriptionConfigurationJsonProtocol extends DefaultJsonProtocol {
  implicit val format = jsonFormat3(SubscriptionConfiguration)
}

package me.snov.sns.model

import spray.json._

case class Subscription(
                         arn: String,
                         owner: String,
                         topicArn: String,
                         protocol: String,
                         endpoint: String
                       )

object Subscription extends DefaultJsonProtocol {
  implicit val format = jsonFormat5(Subscription.apply)
}

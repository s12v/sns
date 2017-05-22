package me.snov.sns.response

import java.util.UUID

import akka.http.scaladsl.model.HttpResponse
import me.snov.sns.model.Subscription

object SubscribeResponse extends XmlHttpResponse {
  def subscribe(subscription: Subscription) = {
    response(
      <SubscribeResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <SubscribeResult>
          <SubscriptionArn>
            {subscription.arn}
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

  def unsubscribe = {
    response(
      <UnsubscribeResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </UnsubscribeResponse>
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
                {subscription.arn}
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

  def listByTopic(subscriptions: Iterable[Subscription]): HttpResponse = {
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
                {subscription.arn}
              </SubscriptionArn>
              <TopicArn>
                {subscription.topicArn}
              </TopicArn>
            </member>
          )}
          </Subscriptions>
        </ListSubscriptionsByTopicResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </ListSubscriptionsByTopicResponse>
    )
  }
}

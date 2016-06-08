package me.snov.sns.response

import java.util.UUID

import me.snov.sns.model.Message

object PublishResponse extends XmlHttpResponse {
  def publish(message: Message) = {
    response(
      <PublishResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <PublishResult>
          <MessageId>{message.uuid}</MessageId>
        </PublishResult>
        <ResponseMetadata>
          <RequestId>{UUID.randomUUID}</RequestId>
        </ResponseMetadata>
      </PublishResponse>
    )
  }
}

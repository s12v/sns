package me.snov.sns.response

import java.util.UUID

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import me.snov.sns.model.Message

object PublishResponse extends XmlHttpResponse {
  def publish(message: Message): HttpResponse = {
    response(
      OK,
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

  def topicNotFound(message: String): HttpResponse = {
    response(
      NotFound,
      <ErrorResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <Error>
          <Code>NotFound</Code>
          <Message>{message}</Message>
        </Error>
        <RequestId>{UUID.randomUUID}</RequestId>
      </ErrorResponse>
    )
  }
}

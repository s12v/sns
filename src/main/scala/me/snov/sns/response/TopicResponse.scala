package me.snov.sns.response

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.HttpResponse
import me.snov.sns.model.Topic

object TopicResponse extends XmlHttpResponse {
  def delete = {
    response(
      OK,
      <DeleteTopicResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </DeleteTopicResponse>
    )
  }

  def create(topic: Topic): HttpResponse = {
    response(
      OK,
      <CreateTopicResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <CreateTopicResult>
          <TopicArn>
            {topic.arn}
          </TopicArn>
        </CreateTopicResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </CreateTopicResponse>
    )
  }

  def list(topics: Iterable[Topic]): HttpResponse = {
    response(
      OK,
      <ListTopicsResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ListTopicsResult>
          <Topics>
            {topics.map(topic =>
            <member>
              <TopicArn>
                {topic.arn}
              </TopicArn>
            </member>
          )}
          </Topics>
        </ListTopicsResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </ListTopicsResponse>
    )
  }
}

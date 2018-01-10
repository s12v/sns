@sqs
Feature: SQS Integration
  # Requires fake sqs: docker run -d -p 9324:9324 s12v/elasticmq

  Background:
    Given AWS SNS client
      And AWS SQS client
      And I purge queue "http://localhost:9324?QueueName=queue1"

  Scenario: Publish
    Given I create a new topic "test1"
    And I subscribe endpoint "file://tmp?fileName=sns.log" with protocol "file" to topic "test1"
    And I subscribe endpoint "aws-sqs://queue1?amazonSQSEndpoint=http://localhost:9324&accessKey=&secretKey=" with protocol "sqs" to topic "test1"
    When I publish a message "Hello, World!" to topic "test1"
    Then The publish request should be successful
    Then I wait for 1 seconds
    Then I should see "Hello, World!" in file "./tmp/sns.log"
    Then I should see "Hello, World!" in queue "http://localhost:9324?QueueName=queue1"

  Scenario: Raw Message Delivery
    Given I create a new topic "test2"
    And I subscribe endpoint "aws-sqs://queue1?amazonSQSEndpoint=http://localhost:9324&accessKey=&secretKey=" with protocol "sqs" to topic "test2" as "subscription"
    And I set "RawMessageDelivery" for "subscription" to "true"
    When I publish a message to topic "test2":
    """
    {
      "hello, world!": true
    }
    """
    Then The publish request should be successful
    Then I wait for 1 seconds
    And I get the message in queue "http://localhost:9324?QueueName=queue1"
    Then the message body should be:
    """
    {
      "hello, world!": true
    }
    """

  Scenario: Message Attributes
    Given I create a new topic "test3"
    And I subscribe endpoint "aws-sqs://queue1?amazonSQSEndpoint=http://localhost:9324&accessKey=&secretKey=" with protocol "sqs" to topic "test3" as "subscription"
    And I set "RawMessageDelivery" for "subscription" to "true"
    When I publish "Hello, World!" to topic "test3" with attributes:
      | Name       | Data Type | String Value |
      | Trace-Id   | String    | 123456       |
      | Logging-Id | String    | EFGADBC      |
    Then The publish request should be successful
    And I wait for 1 seconds
    And I get the message in queue "http://localhost:9324?QueueName=queue1"
    Then the message body should be:
    """
    Hello, World!
    """
    And the message attribute "Trace-Id" should be "123456"
    And the message attribute "Logging-Id" should be "EFGADBC"

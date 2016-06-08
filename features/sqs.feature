@sqs
Feature: SQS Integration
  # Requires fake sqs: docker run -d -p 9234:9324 s12v/elasticmq

  Background:
    Given AWS SNS client
      And AWS SQS client

  Scenario: Publish
    Given I create a new topic "test1"
    And I subscribe endpoint "file://tmp?fileName=sns.log" with protocol "file" to topic "test1"
    And I subscribe endpoint "aws-sqs://queue1?amazonSQSEndpoint=http://localhost:9324&accessKey=&secretKey=" with protocol "sqs" to topic "test1"
    When I publish a message "Hello, World!" to topic "test1"
    Then The publish request should be successful
    Then I wait for 1 seconds
    Then I should see "Hello, World!" in file "./tmp/sns.log"
    Then I should see "Hello, World!" in queue "http://localhost:9324?QueueName=queue1"

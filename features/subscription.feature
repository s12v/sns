@subscription
Feature: Subscription

  Background:
    Given AWS-SDK client
      And Lambda function "lambda"

  Scenario: Subscribe
    Given I create a new topic "cucumber1"
    When I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber1"
    Then subscription should be successful

  Scenario: Subscribe and list
    Given I create a new topic "cucumber1"
    When I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber1"
    And I list subscriptions for topic "cucumber1"
    Then I see endpoint "http://example.com" with topic "cucumber1"

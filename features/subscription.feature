@subscription
Feature: Subscription

  Background:
    Given AWS SNS client

  Scenario: Subscribe
    Given I create a new topic "cucumber1"
    When I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber1"
    Then subscription should be successful

  Scenario: List by topic
    Given I create a new topic "cucumber21"
    And I create a new topic "cucumber22"
    When I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber21"
    And I list subscriptions for topic "cucumber21"
    Then I see endpoint "http://example.com" with topic "cucumber21"
    When I list subscriptions for topic "cucumber22"
    Then I don't see endpoint "http://example.com"

  Scenario: List all topics
    Given I create a new topic "cucumber31"
    And I create a new topic "cucumber32"
    When I subscribe endpoint "http://example1.com" with protocol "http" to topic "cucumber31"
    When I subscribe endpoint "http://example2.com" with protocol "http" to topic "cucumber32"
    And I list all subscriptions
    Then I see endpoint "http://example1.com" with topic "cucumber31"
    And I see endpoint "http://example2.com" with topic "cucumber32"

  Scenario: Unsubscribe
    Given I create a new topic "cucumber4"
    And I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber4" as "subscription"
    And I unsubscribe "subscription"
    Then unsubscription should be successful

  Scenario: Set Subscription Attributes
    Given I create a new topic "cucumber5"
    And I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber5" as "subscription"
    And I set "RawMessageDelivery" for "subscription" to "true"
    And I get subscription attributes for "subscription"
    Then I see attribute "RawMessageDelivry" with value "true"

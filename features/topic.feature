Feature: SNS topics

  Background:
    Given AWS-SDK client

  Scenario: Create topic
    When I create a new topic "cucumber1"
    And I list topics
    Then topic "cucumber1" should exist

  Scenario: Create existing topic
    When I create a new topic "cucumber2"
    And I create a new topic "cucumber2"
    And I list topics
    Then topic "cucumber2" should exist

  Scenario: Delete topic
    Given I create a new topic "cucumber3"
    When I delete topic "cucumber3"
    And I list topics
    Then the topic "cucumber3" should not exist
  

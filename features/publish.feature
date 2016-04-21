@publish
Feature: Publish

  Background:
    Given AWS-SDK client

  Scenario: Publish
    Given I create a new topic "cucumber1"
    And I subscribe endpoint "http://example.com" with protocol "http" to topic "cucumber1"
    When I publish a message "Hello, World!" to topic "cucumber1"
    Then The publish request should be successful

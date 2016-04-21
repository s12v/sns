@publish
Feature: Publish

  Background:
    Given AWS-SDK client

  Scenario: Publish
    Given I create a new topic "cucumber1"
    Given I create a new topic "cucumber2"
    And I subscribe endpoint "file://tmp?fileName=sns21.txt" with protocol "file" to topic "cucumber1"
    And I subscribe endpoint "file://tmp?fileName=sns22.txt" with protocol "file" to topic "cucumber2"
    When I publish a message "Hello, World!" to topic "cucumber1"
    Then The publish request should be successful
    Then I should see "Hello, World" in file "./tmp/sns1.txt"
    Then I should not see "Hello, World" in file "./tmp/sns2.txt"

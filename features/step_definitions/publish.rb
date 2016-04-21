
When(/^I publish a message "([^"]*)" to topic "([^"]*)"$/) do |message, topic|
  @response = $client.publish(topic_arn: get_arn(topic), message: message)
end

Then(/^The publish request should be successful$/) do
  expect(@response.message_id.length).to be > 0
end

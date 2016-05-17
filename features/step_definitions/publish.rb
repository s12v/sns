
When(/^I publish a message "([^"]*)" to topic "([^"]*)"$/) do |message, topic|
  @response = $client.publish(topic_arn: get_arn(topic), message: message)
end

Then(/^The publish request should be successful$/) do
  expect(@response.message_id.length).to be > 0
end

def file_contains_string(file, message)
  File.read(file).index(message)
end

Then(/^I should see "([^"]*)" in file "([^"]*)"$/) do |message, file|
  expect(file_contains_string(file, message)).to be_truthy
end

Then(/^I should not see "([^"]*)" in file "([^"]*)"$/) do |message, file|
  expect(file_contains_string(file, message)).to be_falsey
end

Then(/^I sleep for (\d+) seconds$/) do |n|
  sleep(n.to_i)
end

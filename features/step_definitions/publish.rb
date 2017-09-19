When(/^I publish a message "([^"]*)" to topic "([^"]*)"$/) do |message, topic|
  @response = $SNS.publish(topic_arn: get_arn(topic), message: message)
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

Then(/^I wait for (\d+) seconds$/) do |n|
  sleep(n.to_i)
end

Then(/^I should see "([^"]*)" in queue "([^"]*)"$/) do |message, queue_url|
  options = {
    queue_url: queue_url,
    max_number_of_messages: 10,
    wait_time_seconds: 1,
  }

  response = $SQS.receive_message(options)

  expect(response.messages).to_not be_nil
  expect(response.messages.length).to be >= 0

  found = false
  response.messages.each do |m|
    if m.body.index(message)
      found = true
      $SQS.delete_message({
                            queue_url: queue_url,
                            receipt_handle: m.receipt_handle,
                          })
    end
  end

  expect(found).to be true
end

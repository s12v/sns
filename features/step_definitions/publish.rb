When(/^I publish a message "([^"]*)" to topic "([^"]*)"$/) do |message, topic|
  @response = $SNS.publish(topic_arn: get_arn(topic), message: message)
end

When(/^I publish a message to topic "([^"]*)":?$/) do |topic, message|
  @response = $SNS.publish(topic_arn: get_arn(topic), message: message)
end

When(/^I publish "([^"]*)" to topic "([^"]*)" with attributes:$/) do |message, topic, attribute_table|
  attributes={}
  attribute_table.hashes.each do |row|
    attributes[row['Name']] = {
      data_type: row['Data Type'],
      string_value: row['String Value']
    }
  end
  @response = $SNS.publish(topic_arn: get_arn(topic), message: message, message_attributes: attributes)
end

When(/^I publish a message "([^"]*)" to TopicArn "([^"]*)"$/) do |message, topic_arn|
  begin
    @response = $SNS.publish(topic_arn: topic_arn, message: message)
  rescue => @error
  end
end

Then(/^The publish request should be successful$/) do
  expect(@response.message_id.length).to be > 0
end

Then(/^The publish request should return "([^"]*)" error$/) do |code|
  expect(@error.code).to eq code
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

Given(/^I purge queue "([^"]*)"$/) do |queue_url|
  $SQS.purge_queue({queue_url:queue_url})
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

Then(/^I get the message in queue "([^"]*)"$/) do |queue_url|
  options = {
      queue_url: queue_url,
      max_number_of_messages: 10,
      wait_time_seconds: 1,
      message_attribute_names: ["All"],
  }

  @response = $SQS.receive_message(options)

  expect(@response.messages).to_not be_nil
  expect(@response.messages.length).to be == 1

  @message = @response.messages[0]

  $SQS.delete_message({
                          queue_url: queue_url,
                          receipt_handle: @message.receipt_handle,
                      })
end

Then(/^the message body should be:?$/) do |message|
  expect(@message.body).to be == message
end

Then(/^the message attribute "([^"]*)" should be "([^"]*)"$/) do |name, value|
  expect(@message.message_attributes[name].string_value).to be == value
end
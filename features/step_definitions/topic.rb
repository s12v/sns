
When(/^I create a new topic "([^"]*)"$/) do |topic|
  resp = $client.create_topic name: randomized_topic(topic)
  
  arn = resp.topic_arn
  expect(arn).to be_an_instance_of String 
  expect(arn.length).to be > 0
  expect(arn).to include randomized_topic(topic)
  
  add_arn(topic, arn)
end

When(/^I delete topic "([^"]*)"$/) do |topic|
  arn = get_arn(topic)
  expect(arn).to include(randomized_topic(topic))
  
  $client.delete_topic topic_arn: arn
end

When(/^I list topics$/) do
  @response = $client.list_topics
end

Then(/^topic "([^"]*)" should exist$/) do |topic|
  arn = get_arn(topic)
  expect(arn).to include(randomized_topic(topic))
  
  expect(@response.topics).to_not be_nil
  expect(@response.topics.length).to be > 0
  arns = @response.topics.map {|t| t.topic_arn}
  expect(arns).to include(arn)
end

Then(/^the topic "([^"]*)" should not exist$/) do |topic|
  arn = get_arn(topic)
  expect(arn).to include(randomized_topic(topic))
  
  expect(@response.topics).to_not be_nil
  expect(@response.topics.length).to be >= 0
  arns = @response.topics.map {|t| t.topic_arn}
  expect(arns).not_to include($arn)
end

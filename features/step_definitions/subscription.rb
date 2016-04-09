When(/^I subscribe endpoint "([^"]*)" with protocol "([^"]*)" to topic "([^"]*)"$/) do |endpoint, protocol, topic|
  @response = $client.subscribe({
    topic_arn: get_arn(topic),
    protocol: protocol,
    endpoint: endpoint,
  })
end

Then(/^subscription should be successful$/) do
  p @response.subscription_arn
  expect(@response.subscription_arn.length).to be > 0
end

And(/^I list subscriptions for topic "([^"]*)"$/) do |topic|
  @response = $client.list_subscriptions_by_topic({
    topic_arn: get_arn(topic)  
  })
end

Then(/^I see endpoint "([^"]*)" with topic "([^"]*)"$/) do |endpoint, topic|
  match = @response.subscriptions.select do |subscription|
    subscription.endpoint == endpoint && subscription.topic_arn == get_arn(topic)
  end
  
  expect(match.length).to be > 0
end

Then(/^I see endpoint "([^"]*)"$/) do |endpoint|
  match = @response.subscriptions.select do |subscription|
    subscription.endpoint == endpoint
  end
  
  expect(match.length).to be > 0
end

Then(/^I don't see endpoint "([^"]*)"$/) do |endpoint|
  match = @response.subscriptions.select do |subscription|
    subscription.endpoint == endpoint
  end

  expect(match.length).to be 0
end

And(/^I list all subscriptions$/) do
  @response = $client.list_subscriptions
end

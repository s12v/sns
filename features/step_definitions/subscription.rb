When(/^I subscribe endpoint "([^"]*)" with protocol "([^"]*)" to topic "([^"]*)"(?: as "([^"]*)")?$/) do |endpoint, protocol, topic, name|
  @response = $SNS.subscribe({
    topic_arn: get_arn(topic),
    protocol: protocol,
    endpoint: endpoint,
  })
  if (name)
    @namedSubscriptionArns[name] = @response.subscription_arn
  end
end

Then(/^subscription should be successful$/) do
  expect(@response.subscription_arn.length).to be > 0
end

And(/^I list subscriptions for topic "([^"]*)"$/) do |topic|
  @response = $SNS.list_subscriptions_by_topic({
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
  @response = $SNS.list_subscriptions
end

When(/^I unsubscribe "([^"]*)"$/) do |name|
  @response = $SNS.unsubscribe({
    subscription_arn: @namedSubscriptionArns[name]
  })
end

Then(/^unsubscription should be successful$/) do
  expect(@response.error).to be nil
end

Given("I set {string} for {string} to {string}") do |an, name, av|
   @response = $SNS.set_subscription_attributes({
    subscription_arn: @namedSubscriptionArns[name],
    attribute_value: av,
    attribute_name: an
  })
end

Given("I get subscription attributes for {string}") do |name|
  @response = $SNS.get_subscription_attributes({
    subscription_arn: @namedSubscriptionArns[name]
  })
end

Then("I see attribute {string} with value {string}") do |name, value|
  @response.attributes[name] == value
end

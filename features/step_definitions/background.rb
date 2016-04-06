require 'aws-sdk'
require 'logger'

Before do
  @rand = Random::DEFAULT.rand(999999999999)
  @arns = {}
  @resp = nil
end

def randomized_topic(topic_name)
  "#{topic_name}-#{@rand}"
end

def add_arn(topic, arn)
  @arns[randomized_topic(topic)] = arn
end

def get_arn(topic)
  @arns[randomized_topic(topic)]
end

# LOG=yes for logging
# For live SNS see http://docs.aws.amazon.com/sdkforruby/api/index.html 

Given('AWS-SDK client') do
  config = {
    region: 'us-east-1',
  }

  if ENV.has_key?('SNS_ENDPOINT')
    config[:endpoint] = ENV['SNS_ENDPOINT']
  end

  if ENV.has_key?('LOG') && ENV['LOG'] == 'yes'
    config[:logger] = Logger.new($stdout)
  end
  
  $client = Aws::SNS::Client.new(config)
end

Given(/^Lambda function "([^"]*)"$/) do |lambda|
  $lambda_arn = lambda
end

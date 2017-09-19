require 'aws-sdk'
require 'logger'

Before do
  @rand = Random::DEFAULT.rand(999999999999)
  @arns = {}
  @namedSubscriptionArns = {}
end

After do
  @arns.each do |name, arn|
    puts "Remove topic #{name}"
    begin
      $SNS.delete_topic(topic_arn: arn)
    rescue Aws::SNS::Errors::NotFound
    end
  end
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

Given('AWS SNS client') do
  config = {
    region: 'us-east-1',
  }

  if ENV.has_key?('ENDPOINT')
    config[:endpoint] = ENV['ENDPOINT']
  end

  if ENV.has_key?('HTTP_PROXY')
    config[:http_proxy] = ENV['HTTP_PROXY']
    config[:ssl_verify_peer] = false
  end

  if ENV.has_key?('LOG') && ENV['LOG'] == 'yes'
    config[:logger] = Logger.new($stdout)
  end
  
  $SNS = Aws::SNS::Client.new(config)
end

Given('AWS SQS client') do
  config = {
    region: 'us-east-1',
    endpoint: 'http://localhost:9324',
  }

  $SQS = Aws::SQS::Client.new(config)
end

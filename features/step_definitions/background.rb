require 'aws-sdk'
require 'logger'

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

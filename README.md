[![Build Status](https://travis-ci.org/s12v/sns.svg?branch=master)](https://travis-ci.org/s12v/sns)
# Fake AWS SNS

Example integrations:

 - Amazon SQS: `aws-sqs://queueName[?options]`
 - RabbitMQ: `rabbitmq://hostname[:port]/exchangeName[?options]`
 - HTTP: `http:hostName[:port][/resourceUri][?options]`
 - File: `file://tmp?fileName=sns1.txt`
 - Slack: `slack:@username?webhookUrl=https://hooks.slack.com/services/aaa/bbb/ccc`

See [camel documentation](http://camel.apache.org/components.html) for more deatils

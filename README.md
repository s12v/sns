[![Build Status](https://travis-ci.org/s12v/sns.svg?branch=master)](https://travis-ci.org/s12v/sns)
# SNS

Fake Amazon Simple Notification Service (SNS) for testing. Supports:
 - Create/List/Delete topics
 - Subscribe endpoint
 - Publish message
 - Subscription persistence
 - Integrations with SQS, File, HTTP, RabbitMQ, Slack

## Usage

### Docker

```
docker run -d -p 9911:9911 -e DB_PATH=/tmp/db.json s12v/sns
```

### Direct

Download latest release from https://github.com/s12v/sns/releases and run:
```
DB_PATH=/tmp/db.json java -jar sns-0.0.1.jar
```

## Configuration

Configuration can be set via environment variables:
 - `DB_PATH` - path to database file, default: `db.json`
 - `HTTP_INTERFACE` - interface to bind to, default: `0.0.0.0`
 - `HTTP_PORT` - tcp port, default: `9911`

## Example fake SQS integration:

Database `db.json`:
```json
{
  "version": 1,
  "timestamp": 1465414804110,
  "subscriptions": [
    {
      "arn": "subscription-arn1",
      "topicArn": "arn:aws:sns:us-east-1:1465414804035:test1",
      "endpoint": "aws-sqs://queue1?amazonSQSEndpoint=http://localhost:9324&accessKey=&secretKey=",
      "owner": "",
      "protocol": "sqs"
    },
    {
      "arn": "subscription-arn2",
      "topicArn": "arn:aws:sns:us-east-1:1465414804035:test1",
      "endpoint": "file:///tmp?fileName=sns.log",
      "owner": "",
      "protocol": "file"
    }
  ],
  "topics": [
    {
      "arn": "arn:aws:sns:us-east-1:1465414804035:test1",
      "name": "test1"
    }
  ]
}
```

## Supported integrations

 - Amazon SQS: `aws-sqs://queueName?amazonSQSEndpoint=...&accessKey=&secretKey=`
 - RabbitMQ: `rabbitmq://hostname[:port]/exchangeName[?options]`
 - HTTP: `http:hostName[:port][/resourceUri][?options]`
 - File: `file://tmp?fileName=sns1.txt`
 - Slack: `slack:@username?webhookUrl=https://hooks.slack.com/services/aaa/bbb/ccc`

See [camel documentation](http://camel.apache.org/components.html) for more details

## Development

Unit tests: `sbt test`

It's tested with real AWS Ruby and PHP SDKs.

Start elasticmq for SQS integration test
```
docker run -d -p 9324:9324 s12v/elasticmq
```

Ruby SDK tests:
```
bundle install
ENDPOINT=http://localhost:9911 bundle exec cucumber
```

PHP SDK tests:
```
composer install
./bin/behat
```

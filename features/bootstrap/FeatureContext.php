<?php

use Aws\Result;
use Aws\Sns\SnsClient;
use Behat\Behat\Context\BehatContext;
use Behat\Behat\Exception\PendingException;

class FeatureContext extends BehatContext
{
    /**
     * @var SnsClient
     */
    private $sns;

    /**
     * @var string[]
     */
    private $arns;

    /**
     * @var int
     */
    private $seed;

    /**
     * @var Result
     */
    private $result;
    
    public function __construct()
    {
        $this->seed = mt_rand();
    }

    /**
     * @Given /^AWS-SDK client$/
     */
    public function awsSdkClient()
    {
        $config = [
            'version' => 'latest',
            'region' => 'us-east-1',
        ];

        if (getenv('ENDPOINT')) {
            $config['endpoint'] = getenv('ENDPOINT');
        }

//        if (getenv('HTTP_PROXY')) {
//            $config['http'] = [
//                'proxy' => 'http://192.168.16.1:10',
//                'verify' => false,
//            ];
//        }

        $this->sns = new SnsClient($config);
    }

    /**
     * @param string $name
     * @return string
     */
    private function getRandomizedTopicName($name)
    {
        return sprintf("%s-%s", $name, $this->seed);
    }

    /**
     * @param string $name
     * @return string
     */
    private function getArnByName($name)
    {
        if (!array_key_exists($name, $this->arns)) {
            throw new \RuntimeException("Unknown topic $name");
        }
        
        return $this->arns[$name];
    }
    
    /**
     * @Given /^I create a new topic "([^"]*)"$/
     * @param string $name
     */
    public function iCreateANewTopic($name)
    {
        $this->result = $this->sns->createTopic(['Name' => $this->getRandomizedTopicName($name)]);

        PHPUnit_Framework_Assert::assertTrue($this->result->hasKey('TopicArn'));

        $this->arns[$name] = $this->result->get('TopicArn');
    }

    /**
     * @Given /^I subscribe endpoint "([^"]*)" with protocol "([^"]*)" to topic "([^"]*)"$/
     */
    public function iSubscribeEndpointWithProtocolToTopic($endpoint, $protocol, $name)
    {
        $this->sns->subscribe([
            'Endpoint' => $endpoint,
            'Protocol' => $protocol,
            'TopicArn' => $this->getArnByName($name),
        ]);
    }

    /**
     * @When /^I publish a message "([^"]*)" to topic "([^"]*)"$/
     */
    public function iPublishAMessageToTopic($arg1, $arg2)
    {
        throw new PendingException();
    }

    /**
     * @Then /^The publish request should be successful$/
     */
    public function thePublishRequestShouldBeSuccessful()
    {
        throw new PendingException();
    }

    /**
     * @Then /^I should see "([^"]*)" in file "([^"]*)"$/
     */
    public function iShouldSeeInFile($arg1, $arg2)
    {
        throw new PendingException();
    }

    /**
     * @Then /^I should not see "([^"]*)" in file "([^"]*)"$/
     */
    public function iShouldNotSeeInFile($arg1, $arg2)
    {
        throw new PendingException();
    }

    /**
     * @Then /^subscription should be successful$/
     */
    public function subscriptionShouldBeSuccessful()
    {
        throw new PendingException();
    }

    /**
     * @Given /^I list subscriptions for topic "([^"]*)"$/
     */
    public function iListSubscriptionsForTopic($name)
    {
        throw new PendingException();
    }

    /**
     * @Then /^I see endpoint "([^"]*)" with topic "([^"]*)"$/
     */
    public function iSeeEndpointWithTopic($arg1, $arg2)
    {
        throw new PendingException();
    }

    /**
     * @Then /^I don\'t see endpoint "([^"]*)"$/
     */
    public function iDonTSeeEndpoint($arg1)
    {
        throw new PendingException();
    }

    /**
     * @Given /^I list all subscriptions$/
     */
    public function iListAllSubscriptions()
    {
        $this->result = $this->sns->listSubscriptions();

        PHPUnit_Framework_Assert::assertNotEmpty($this->result);
    }

    /**
     * @Given /^I list topics$/
     */
    public function iListTopics()
    {
        $this->result = $this->sns->listTopics();

        PHPUnit_Framework_Assert::assertNotEmpty($this->result);
    }

    /**
     * @Then /^topic "([^"]*)" should exist$/
     */
    public function topicShouldExist($name)
    {
        PHPUnit_Framework_Assert::assertTrue($this->result->hasKey('Topics'));
        $topics = $this->result->get('Topics');
        PHPUnit_Framework_Assert::assertTrue(is_array($topics));
        PHPUnit_Framework_Assert::assertNotEmpty($topics);
        
        $arn = $this->getArnByName($name);
        $filtered = array_filter($topics, function ($topic) use ($arn) {
            return $topic['TopicArn'] === $arn;
        });
        PHPUnit_Framework_Assert::assertNotEmpty($filtered);
    }

    /**
     * @When /^I delete topic "([^"]*)"$/
     */
    public function iDeleteTopic($name)
    {
        $this->sns->deleteTopic(['TopicArn' => $this->getArnByName($name)]);
    }

    /**
     * @Then /^the topic "([^"]*)" should not exist$/
     */
    public function theTopicShouldNotExist($name)
    {
        PHPUnit_Framework_Assert::assertTrue($this->result->hasKey('Topics'));
        $topics = $this->result->get('Topics');
        PHPUnit_Framework_Assert::assertTrue(is_array($topics));
        PHPUnit_Framework_Assert::assertNotEmpty($topics);

        $arn = $this->getArnByName($name);
        $filtered = array_filter($topics, function ($topic) use ($arn) {
            return $topic['TopicArn'] === $arn;
        });
        PHPUnit_Framework_Assert::assertEmpty($filtered);
    }

    /**
     * @Then /^I wait for (\d+) seconds$/
     */
    public function iSleepForSecond($seconds)
    {
        sleep($seconds);
    }
}

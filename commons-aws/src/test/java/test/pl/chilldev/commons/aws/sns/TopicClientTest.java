/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.aws.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pl.chilldev.commons.aws.sns.TopicClient;

public class TopicClientTest
{
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock
    private AmazonSNS sns;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void publish() throws JsonProcessingException
    {
        // this is to make sure we resolve the AWS region for default region provider
        this.environmentVariables.set("AWS_REGION", "eu-central-1");

        // just for code coverage
        new TopicClient(this.objectMapper, null);

        String topic = "arn:test";
        Object input = new Object();
        String message = "{}";
        PublishResult result = new PublishResult();

        TopicClient client = new TopicClient(this.sns, this.objectMapper, topic);

        Mockito.when(this.objectMapper.writeValueAsString(input)).thenReturn(message);
        Mockito.when(this.sns.publish(topic, message)).thenReturn(result);

        Assert.assertSame(
            "TopicClient.publish() should send serialized message to configured topic.",
            result,
            client.publish(input)
        );
    }
}

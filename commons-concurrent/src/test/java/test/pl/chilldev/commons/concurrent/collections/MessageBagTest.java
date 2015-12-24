/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.concurrent.collections;

import java.time.Instant;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.concurrent.collections.MessageBag;

public class MessageBagTest
{
    @Test
    public void addMessage()
        throws
            InterruptedException
    {
        String key = "foo";
        String value = "bar";

        MessageBag messages = new MessageBag();

        Instant start = Instant.now();
        messages.addMessage(key, value);
        Instant end = Instant.now();

        MessageBag.Message message = messages.getMessages(key).get(0);
        Instant createdAt = message.getCreatedAt();

        Assert.assertEquals(
            "MessageBag.addMessage() should create internally message with defined content.",
            value,
            message.getContent()
        );
        Assert.assertFalse(
            "MessageBag.Message constructor should initialize message with current date.",
            createdAt.isBefore(start)
        );
        Assert.assertFalse(
            "MessageBag.Message constructor should initialize message with current date.",
            createdAt.isAfter(end)
        );
    }

    @Test
    public void hasMessages()
    {
        MessageBag messages = new MessageBag();

        Assert.assertFalse("MessageBag.hasMessages() should return overall state of the container.", messages.hasMessages());

        messages.addMessage("foo", "bar");

        Assert.assertTrue("MessageBag.hasMessages() should return overall state of the container.", messages.hasMessages());
    }

    @Test
    public void hasMessagesByType()
    {
        String key = "foo";
        String otherKey = "baz";

        MessageBag messages = new MessageBag();

        Assert.assertFalse(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(key)
        );
        Assert.assertFalse(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(otherKey)
        );

        messages.addMessage(key, "bar");

        Assert.assertTrue(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(key)
        );
        Assert.assertFalse(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(otherKey)
        );
    }

    @Test
    public void getMessages()
    {
        String key = "foo";
        String value = "bar";

        MessageBag messages = new MessageBag();
        messages.addMessage(key, value);

        List<MessageBag.Message> list = messages.getMessages(key);
        Assert.assertEquals("MessageBag.getMessages() should return list of messages of given type.", 1, list.size());
        Assert.assertEquals(
            "MessageBag.getMessages() should return list of messages of given type.",
            value,
            list.get(0).getContent()
        );

        list = messages.getMessages(key);
        Assert.assertTrue("MessageBag.getMessages() should clean sotrage for given type.", list.isEmpty());
    }

    @Test
    public void getMessagesEmpty()
    {
        MessageBag messages = new MessageBag();
        List<MessageBag.Message> list = messages.getMessages("foo");
        Assert.assertTrue(
            "MessageBag.getMessages() should return empty list if there are no messages of given type.",
            list.isEmpty()
        );
    }

    @Test
    public void getAllMessages()
    {
        String key = "foo";
        String value = "bar";

        MessageBag messages = new MessageBag();
        messages.addMessage(key, value);

        Map<String, List<MessageBag.Message>> map = messages.getAllMessages();
        Assert.assertEquals(
            "MessageBag.getAllMessages() should return list of all messages (groupped by type).",
            1,
            map.size()
        );
        Assert.assertEquals(
            "MessageBag.getAllMessages() should return list of all messages (groupped by type).",
            1,
            map.get(key).size()
        );
        Assert.assertEquals(
            "MessageBag.getAllMessages() should return list of all messages.",
            value,
            map.get(key).get(0).getContent()
        );

        Assert.assertFalse("MessageBag.getAllMessages() should clean sotrage for given type.", messages.hasMessages());
    }

    @Test
    public void getAllMessagesEmpty()
    {
        MessageBag messages = new MessageBag();
        Map<String, List<MessageBag.Message>> map = messages.getAllMessages();
        Assert.assertTrue(
            "MessageBag.getAllMessages() should return empty map if there are no messages.",
            map.isEmpty()
        );
    }
}

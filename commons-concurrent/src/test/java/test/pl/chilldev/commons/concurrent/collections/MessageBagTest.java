/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @author Rafał Wrzeszcz <rafal.wrzeszcz@wrzasq.pl>
 * @copyright 2014 © by Rafał Wrzeszcz - Wrzasq.pl.
 * @version 0.0.2
 * @since 0.0.2
 * @category ChillDev-Commons
 * @subcategory Concurrent
 */

package test.pl.chilldev.commons.concurrent.collections;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

        Date start = new Date();
        messages.addMessage(key, value);
        Date end = new Date();

        MessageBag.Message message = messages.getMessages(key).get(0);
        Date createdAt = message.getCreatedAt();

        assertEquals(
            "MessageBag.addMessage() should create internally message with defined content.",
            value,
            message.getContent()
        );
        assertFalse(
            "MessageBag.Message constructor should initialize message with current date.",
            createdAt.before(start)
        );
        assertFalse(
            "MessageBag.Message constructor should initialize message with current date.",
            createdAt.after(end)
        );
    }

    @Test
    public void hasMessages()
    {
        MessageBag messages = new MessageBag();

        assertFalse("MessageBag.hasMessages() should return overall state of the container.", messages.hasMessages());

        messages.addMessage("foo", "bar");

        assertTrue("MessageBag.hasMessages() should return overall state of the container.", messages.hasMessages());
    }

    @Test
    public void hasMessagesByType()
    {
        String key = "foo";
        String otherKey = "baz";

        MessageBag messages = new MessageBag();

        assertFalse(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(key)
        );
        assertFalse(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(otherKey)
        );

        messages.addMessage(key, "bar");

        assertTrue(
            "MessageBag.hasMessages(type) should return state of the container for given message type.",
            messages.hasMessages(key)
        );
        assertFalse(
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
        assertEquals("MessageBag.getMessages() should return list of messages of given type.", 1, list.size());
        assertEquals(
            "MessageBag.getMessages() should return list of messages of given type.",
            value,
            list.get(0).getContent()
        );

        list = messages.getMessages(key);
        assertTrue("MessageBag.getMessages() should clean sotrage for given type.", list.isEmpty());
    }

    @Test
    public void getMessagesEmpty()
    {
        MessageBag messages = new MessageBag();
        List<MessageBag.Message> list = messages.getMessages("foo");
        assertTrue(
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
        assertEquals(
            "MessageBag.getAllMessages() should return list of all messages (groupped by type).",
            1,
            map.size()
        );
        assertEquals(
            "MessageBag.getAllMessages() should return list of all messages (groupped by type).",
            1,
            map.get(key).size()
        );
        assertEquals(
            "MessageBag.getAllMessages() should return list of all messages.",
            value,
            map.get(key).get(0).getContent()
        );

        assertFalse("MessageBag.getAllMessages() should clean sotrage for given type.", messages.hasMessages());
    }

    @Test
    public void getAllMessagesEmpty()
    {
        MessageBag messages = new MessageBag();
        Map<String, List<MessageBag.Message>> map = messages.getAllMessages();
        assertTrue(
            "MessageBag.getAllMessages() should return empty map if there are no messages.",
            map.isEmpty()
        );
    }
}

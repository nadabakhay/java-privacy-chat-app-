package com.example.networkchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatMessageUtilTest {

    @Test
    public void testFormatMessageUsesSenderAndText() {
        String formatted = ChatMessageUtil.formatMessage("Alice", "hello");
        assertEquals("Alice: hello", formatted);
    }

    @Test
    public void testNormalizePortFallsBackForInvalidInput() {
        assertEquals(9090, ChatMessageUtil.parsePort("bad", 9090));
        assertEquals(1234, ChatMessageUtil.parsePort("1234", 9090));
    }

    @Test
    public void testNormalizeUserNameHandlesBlankValues() {
        assertTrue(ChatMessageUtil.normalizeUserName("   ").startsWith("Guest"));
    }
}

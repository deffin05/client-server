package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class EncoderTest {
    @Test
    void testEncode() {
        Encoder encoder = new Encoder();
        Message message = new Message(1, 86, "Hello, World!");
        Package pkg = new Package((byte) 36, 123456789L, message);

        ByteBuffer buffer = encoder.encode(pkg);
        // You can add assertions here to verify the contents of the buffer

        assertEquals(buffer.get(0), (byte) 0x13);
    }
}

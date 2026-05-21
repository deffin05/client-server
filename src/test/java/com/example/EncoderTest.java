package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class EncoderTest {
    @Test
    void testEncode() {
        Encoder encoder = new Encoder();
        Message message = new Message(1, 86, "test123");
        Package pkg = new Package((byte) 36, 123456789L, message);

        byte[] encoded = encoder.encode(pkg);
        ByteBuffer buffer = ByteBuffer.allocate(encoded.length);

        buffer.put((byte) 0x13);
        buffer.put((byte) 36);
        buffer.putLong(123456789L);

        int msgLength = message.getMessage().getBytes().length + 4 + 4;
        buffer.putInt(msgLength);

        short infoCrc = Crc16.calculateCrc(buffer.array(), 0, 14);
        buffer.putShort(infoCrc);

        buffer.putInt(1);
        buffer.putInt(86);
        buffer.put(message.getMessage().getBytes());

        short msgCrc = Crc16.calculateCrc(buffer.array(), 16, msgLength);
        buffer.putShort(msgCrc);

        assertArrayEquals(encoded, buffer.array());
    }
}

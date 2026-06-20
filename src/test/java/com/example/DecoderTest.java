package com.example;

import static org.junit.jupiter.api.Assertions.*;

import com.example.decryptor.DefaultDecryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DecoderTest {
    private DefaultDecryptor decoder;
    BlockingQueue<NetworkPackage<byte[]>> inputQueue;
    BlockingQueue<NetworkPackage<Package>> outputQueue;
    private final byte[] VALID_ARRAY = new byte[]{
            0x13,                           // Magic number
            62,                             // Source
            0, 0, 0, 0, 0, 0, 0, 100,       // Packet id
            0, 0, 0, 24,                    // Message length
            0x6f, 0x34,                     // Info CRC
            0, 0, 0, 3,                     // Message type
            0, 0, 0, 127,                   // User id
            81, 49, 25, -101,
            113, 83, 12, -99,
            -110, 112, 65, 45,
            55, 121, -103, 46,              // Message = "321test" encrypted with AES
            0x74, 0x05                      // Message CRC
    };

    @BeforeEach
    void setup() {
        inputQueue = new LinkedBlockingQueue<>();
        outputQueue = new LinkedBlockingQueue<>();
        decoder = new DefaultDecryptor(inputQueue, outputQueue);
    }

    @Test
    void testDecode() {
        assertDoesNotThrow(() -> decoder.decrypt(VALID_ARRAY));
        Package decodedPackage = assertDoesNotThrow(() -> outputQueue.take().getData());
        Message decodedMessage = decodedPackage.getMessage();

        assertEquals(62, decodedPackage.getbSrc());
        assertEquals(100, decodedPackage.getbPktId());

        assertEquals(3, decodedMessage.getcType());
        assertEquals(127, decodedMessage.getbUserId());
        assertEquals("321test", decodedMessage.getMessage());
    }

    @Test
    void testDecodeInvalidMagicNumber() {
        byte[] invalid_array = VALID_ARRAY.clone();
        invalid_array[0] = 0x3F;

        assertThrows(RuntimeException.class, () -> decoder.decrypt(invalid_array));
    }

    @Test
    void testDecodeInvalidInfoCRC() {
        byte[] invalid_array = VALID_ARRAY.clone();
        invalid_array[14] = 0;

        assertThrows(RuntimeException.class, () -> decoder.decrypt(invalid_array));
    }

    @Test
    void testDecodeInvalidMessageCRC() {
        byte[] invalid_array = VALID_ARRAY.clone();
        invalid_array[invalid_array.length - 1] = 0;

        assertThrows(RuntimeException.class, () -> decoder.decrypt(invalid_array));
    }
}

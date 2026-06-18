package com.example;

import static org.junit.jupiter.api.Assertions.*;

import com.example.encryptor.DefaultEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncoderTest {
    private DefaultEncryptor encoder;
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
        encoder = new DefaultEncryptor();
    }

    @Test
    void testEncode() {
        Message message = new Message(3, 127, "321test");
        Package pkg = new Package((byte) 62, 100, message);

        byte[] encoded = encoder.encode(pkg);
        assertArrayEquals(VALID_ARRAY, encoded);
    }
}

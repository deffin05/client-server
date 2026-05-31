package com.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DecoderTest {

    private final byte[] VALID_ARRAY = new byte[]{
            0x13,                           // Magic number
            62,                             // Source
            0, 0, 0, 0, 0, 0, 0, 100,       // Packet id
            0, 0, 0, 15,                    // Message length
            0x61, 0x74,                     // Info CRC
            0, 0, 0, 3,                     // Message type
            0, 0, 0, 127,                   // User id
            51, 50, 49, 116, 101, 115, 116, // Message = "321test"
            0x4D, 0x17                      // Message CRC
    };

    @Test
    void testDecode() {
        Decoder decoder = new Decoder();
        Message expectedMessage = new Message(3, 127, "321test");
        Package expectedPackage = new Package((byte) 62, 100L, expectedMessage);


        Package decodedPackage = assertDoesNotThrow(() -> decoder.decode(VALID_ARRAY));
        Message decodedMessage = decodedPackage.getMessage();

        assertEquals(expectedPackage.getbSrc(), decodedPackage.getbSrc());
        assertEquals(expectedPackage.getbPktId(), decodedPackage.getbPktId());

        assertEquals(expectedMessage.getcType(), decodedMessage.getcType());
        assertEquals(expectedMessage.getbUserId(), decodedMessage.getbUserId());
        assertEquals(expectedMessage.getMessage(), decodedMessage.getMessage());
    }
}

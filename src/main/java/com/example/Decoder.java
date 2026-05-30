package com.example;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Decoder {
    private static final byte MAGIC_NUMBER = 0x13;

    public Package decode(byte[] arr) throws Exception {
        if (arr == null || arr.length < 26)
            throw new IllegalArgumentException("The package in invalid");

        ByteBuffer buffer = ByteBuffer.wrap(arr);

        if (buffer.get() != MAGIC_NUMBER)
            throw new Exception("The magic number does not match");

        byte bSrc = buffer.get();
        long bPktId = buffer.getLong();
        int msgLength = buffer.getInt();

        short infoCrcReceived = buffer.getShort();
        short infoCrcCalculated = Crc16.calculateCrc(arr, 0, 14);

        if (infoCrcReceived != infoCrcCalculated)
            throw new Exception("Info CRC does not match.");

        int cType = buffer.getInt();
        int bUserId = buffer.getInt();

        byte[] messageArray = new byte[msgLength - 8];
        buffer.get(messageArray);
        String message = Arrays.toString(messageArray);

        short msgCrcReceived = buffer.getShort();
        short msgCrcCalculated = Crc16.calculateCrc(arr, 16, msgLength);

        if (msgCrcReceived != msgCrcCalculated)
            throw new Exception("Message CRC does not match.");

        return new Package(bSrc, bPktId, new Message(cType, bUserId, message));
    }
}

package com.example;

import java.nio.ByteBuffer;

public class Encoder {
    public Encoder() {

    }

    public ByteBuffer encode(Package pkg) {
        int msgLength = pkg.getMessage().getMessage().getBytes().length + 4 + 4;
        int length = 1 + 1 + 8 + 4 + 2 + 2 + msgLength;
        ByteBuffer buffer = ByteBuffer.allocate(length);

        buffer.put((byte) 0x13);
        buffer.put(pkg.getbSrc());
        buffer.putLong(pkg.getbPktId());
        buffer.putInt(msgLength);
        short infoCrc = Crc16.calculateCrc(buffer.array(), 0, 14);
        buffer.putShort(infoCrc);

        buffer.putInt(pkg.getMessage().getcType());
        buffer.putInt(pkg.getMessage().getbUserId());
        buffer.put(pkg.getMessage().getMessage().getBytes());

        short msgCrc = Crc16.calculateCrc(buffer.array(), 16, msgLength);
        buffer.putShort(msgCrc);

        buffer.flip();
        return buffer;
    }
}

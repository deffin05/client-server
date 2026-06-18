package com.example.encryptor;

import com.example.Crc16;
import com.example.Package;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DefaultEncryptor {
    public byte[] encode(Package pkg) {
        byte[] encryptedMessage = encrypt(pkg.getMessage().getMessage());
        int msgLength = encryptedMessage.length + 4 + 4;
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
        buffer.put(encryptedMessage);

        short msgCrc = Crc16.calculateCrc(buffer.array(), 16, msgLength);
        buffer.putShort(msgCrc);

        return buffer.array();
    }

    private byte[] encrypt(String message) {
        String KEY_BASE64 = "3pK22raz7n2/lyedxsQP2g==";

        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] keyBytes = Base64.getDecoder().decode(KEY_BASE64);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(message.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}

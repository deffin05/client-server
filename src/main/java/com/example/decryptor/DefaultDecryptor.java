package com.example.decryptor;

import com.example.Crc16;
import com.example.Message;
import com.example.Package;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DefaultDecryptor implements Decryptor{
    private static final byte MAGIC_NUMBER = 0x13;

    public void decrypt(byte[] arr) {
        if (arr == null || arr.length < 26)
            throw new IllegalArgumentException("The package in invalid");

        ByteBuffer buffer = ByteBuffer.wrap(arr);

        if (buffer.get() != MAGIC_NUMBER)
            throw new RuntimeException("The magic number does not match");

        byte bSrc = buffer.get();
        long bPktId = buffer.getLong();
        int msgLength = buffer.getInt();

        short infoCrcReceived = buffer.getShort();
        short infoCrcCalculated = Crc16.calculateCrc(arr, 0, 14);

        if (infoCrcReceived != infoCrcCalculated)
            throw new RuntimeException("Info CRC does not match.");

        int cType = buffer.getInt();
        int bUserId = buffer.getInt();

        byte[] messageArray = new byte[msgLength - 8];
        buffer.get(messageArray);
        String message = decipher(messageArray);

        short msgCrcReceived = buffer.getShort();
        short msgCrcCalculated = Crc16.calculateCrc(arr, 16, msgLength);

        if (msgCrcReceived != msgCrcCalculated)
            throw new RuntimeException("Message CRC does not match.");

        Package result = new Package(bSrc, bPktId, new Message(cType, bUserId, message));
    }

    private String decipher(byte[] arr) {
        String KEY_BASE64 = "3pK22raz7n2/lyedxsQP2g==";

        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] keyBytes = Base64.getDecoder().decode(KEY_BASE64);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(arr));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}

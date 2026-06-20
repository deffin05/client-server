package com.example.encryptor;

import com.example.Crc16;
import com.example.Package;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;

public class DefaultEncryptor implements Encryptor, Runnable {
    private static final byte MAGIC_NUMBER = 0x13;
    private final BlockingQueue<Package> inputQueue;
    private final BlockingQueue<byte[]> outputQueue;

    public DefaultEncryptor(BlockingQueue<Package> inputQueue, BlockingQueue<byte[]> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }


    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Package pkg = inputQueue.take();
                byte[] encryptedPkg = encrypt(pkg);
                outputQueue.put(encryptedPkg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public byte[] encrypt(Package pkg) {
        byte[] encryptedMessage = applyCipher(pkg.getMessage().getMessage());
        int msgLength = encryptedMessage.length + 4 + 4;
        int length = 1 + 1 + 8 + 4 + 2 + 2 + msgLength;
        ByteBuffer buffer = ByteBuffer.allocate(length);

        buffer.put(MAGIC_NUMBER);
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

    private byte[] applyCipher(String message) {
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

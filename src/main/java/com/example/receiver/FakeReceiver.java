package com.example.receiver;

import com.example.Message;
import com.example.Package;
import com.example.encryptor.DefaultEncryptor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class FakeReceiver implements Receiver, Runnable {
    private final BlockingQueue<byte[]> outputQueue;
    private DefaultEncryptor encryptor;
    private int currentMessage = 0;

    public FakeReceiver(BlockingQueue<byte[]> outputQueue) {
        this.outputQueue = outputQueue;
        encryptor = new DefaultEncryptor(null, null);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                receiveMessage();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public void receiveMessage() throws InterruptedException {
        Thread.sleep(200 + ThreadLocalRandom.current().nextInt(0, 500));
        byte[] message = doReceiveMessage();
        outputQueue.put(message);
    }

    private byte[] doReceiveMessage() {
        Package[] pkgs = {new Package((byte) 13, 100L, new Message(1, 20, "")),
                new Package((byte) 13, 100L, new Message(2, 20, "")),
                new Package((byte) 13, 100L, new Message(3, 20, ""))
        };
        currentMessage++;
        return encryptor.encrypt(pkgs[currentMessage % 3]);
    }
}

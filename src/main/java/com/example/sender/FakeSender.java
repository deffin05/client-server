package com.example.sender;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

public class FakeSender implements Sender, Runnable {
    private final BlockingQueue<byte[]> inputQueue;

    public FakeSender(BlockingQueue<byte[]> inputQueue) {
        this.inputQueue = inputQueue;
    }

    @Override
    public void run() {
        try {
            byte[] message = inputQueue.take();
            sendMessage(message, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendMessage(byte[] message, InetAddress target) {
        System.out.println("Message sent");
    }
}

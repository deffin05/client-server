package com.example.sender;

import com.example.Package;
import com.example.decryptor.DefaultDecryptor;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FakeSender implements Sender, Runnable {
    private final BlockingQueue<byte[]> inputQueue;
    private final BlockingQueue<Package> printQueue;
    private DefaultDecryptor decryptor;

    public FakeSender(BlockingQueue<byte[]> inputQueue) {
        this.inputQueue = inputQueue;
        this.printQueue = new LinkedBlockingQueue<>();
        DefaultDecryptor decryptor = new DefaultDecryptor(null, printQueue);
    }
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] message = inputQueue.take();
                sendMessage(message, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendMessage(byte[] message, InetAddress target) {
        try {
            decryptor.decrypt(message);
            Package printPkg = printQueue.take();
            System.out.println("Sent message: " + printPkg);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

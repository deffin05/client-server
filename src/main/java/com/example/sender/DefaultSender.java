package com.example.sender;

import com.example.NetworkPackage;
import com.example.Package;
import com.example.decryptor.DefaultDecryptor;
import com.example.network.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultSender implements Sender, Runnable {
    private final BlockingQueue<NetworkPackage<byte[]>> inputQueue;
    private Connection conn;

    public DefaultSender(BlockingQueue<NetworkPackage<byte[]>> inputQueue) {
        this.inputQueue = inputQueue;
    }
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                NetworkPackage<byte[]> message = inputQueue.take();
                conn = message.getConnection();
                sendMessage(message.getData());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendMessage(byte[] message) {
        try {
            conn.sendResponse(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

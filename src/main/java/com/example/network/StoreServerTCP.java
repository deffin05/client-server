package com.example.network;

import com.example.NetworkPackage;
import com.example.receiver.Receiver;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class StoreServerTCP implements Runnable, Receiver{
    public static final int PORT = 13285;
    private final BlockingQueue<NetworkPackage<byte[]>> outputQueue;

    public StoreServerTCP(BlockingQueue<NetworkPackage<byte[]>> outputQueue) {
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    receiveMessage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public void receiveMessage() throws InterruptedException, IOException {
        try (ServerSocket s = new ServerSocket(PORT)) {
            System.out.println("TCP server started: " + s);
            while (true) {
                Socket socket = s.accept();
                new TCPClientHandler(socket, outputQueue);
            }
        }
    }
}

class TCPClientHandler extends Thread {
    private final Socket socket;
    private final BlockingQueue<NetworkPackage<byte[]>> outputQueue;

    TCPClientHandler(Socket socket, BlockingQueue<NetworkPackage<byte[]>> outputQueue) {
        this.socket = socket;
        this.outputQueue = outputQueue;

        start();
    }


    @Override
    public void run() {
        try {
            InputStream in = socket.getInputStream();
            byte[] arr = in.readAllBytes();
            outputQueue.put(new NetworkPackage<byte[]>(arr, new TCPConnection(socket)));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from TCP connection");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }
}
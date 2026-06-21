package com.example.network;

import com.example.NetworkPackage;
import com.example.receiver.Receiver;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StoreServerTCP implements Runnable, Receiver {
    public static final int PORT = 13285;
    private final BlockingQueue<NetworkPackage<byte[]>> outputQueue;
    private final ServerSocket serverSocket;
    private final ExecutorService clientThreadExecutor;

    public StoreServerTCP(BlockingQueue<NetworkPackage<byte[]>> outputQueue) {
        this.outputQueue = outputQueue;
        try {
            this.serverSocket = new ServerSocket(PORT);
            System.out.println("TCP server started: " + serverSocket);
            clientThreadExecutor = Executors.newCachedThreadPool();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't start the TCP server");
        }
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
        Socket socket = serverSocket.accept();
        System.out.println("New connection: " + socket);
        clientThreadExecutor.submit(new TCPClientHandler(socket, outputQueue));
    }

    public void close() {
        System.out.println("Shutting down TCP server");
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }

        clientThreadExecutor.shutdown();
        try {
            if (!clientThreadExecutor.awaitTermination(3, TimeUnit.SECONDS)) clientThreadExecutor.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class TCPClientHandler implements Runnable {
    private final Socket socket;
    private final BlockingQueue<NetworkPackage<byte[]>> outputQueue;
    private final DataInputStream in;

    TCPClientHandler(Socket socket, BlockingQueue<NetworkPackage<byte[]>> outputQueue) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        this.outputQueue = outputQueue;
    }


    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int requestLength = in.readInt();
                byte[] arr = new byte[requestLength];
                in.readFully(arr);
                outputQueue.put(new NetworkPackage<>(arr, new TCPConnection(socket)));
            }
        } catch (EOFException e) {
            System.out.println("Client closed the connection");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
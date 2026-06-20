package com.example.network;

import com.example.NetworkPackage;
import com.example.receiver.Receiver;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class StoreServerTCP implements Runnable, Receiver {
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
                System.out.println("New connection: " + socket);
                new TCPClientHandler(socket, outputQueue);
            }
        }
    }
}

class TCPClientHandler extends Thread {
    private final Socket socket;
    private final BlockingQueue<NetworkPackage<byte[]>> outputQueue;
    private final DataInputStream in;

    TCPClientHandler(Socket socket, BlockingQueue<NetworkPackage<byte[]>> outputQueue) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        this.outputQueue = outputQueue;

        start();
    }


    @Override
    public void run() {
        try {
            while (true) {
                int requestLength = in.readInt();
                if (requestLength == -1) {
                    // End connection
                    break;
                }
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
            } catch (IOException ignored) {}
        }
    }
}
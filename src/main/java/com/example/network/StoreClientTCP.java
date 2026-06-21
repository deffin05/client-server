package com.example.network;

import com.example.Message;
import com.example.Package;
import com.example.decryptor.DefaultDecryptor;
import com.example.encryptor.DefaultEncryptor;
import com.example.encryptor.Encryptor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreClientTCP extends Thread {
    private Socket socket;
    private static AtomicInteger last_id = new AtomicInteger(1);
    private int id;
    boolean isConnected = false;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            StoreClientTCP client = new StoreClientTCP();
            client.start();
        }
    }

    @Override
    public void run() {
        id = last_id.incrementAndGet();
        for (int i = 0; i < 20; i++) {
            Package response = send(new Package((byte) 1, i, new Message(1, id, "")));
        }
        close();
    }

    public void connect() {
        InetAddress addr = InetAddress.getLoopbackAddress();
        int retries = 0;

        while (!isConnected && !Thread.currentThread().isInterrupted()) {
            try {
                socket = new Socket(addr, StoreServerTCP.PORT);
                isConnected = true;
                System.out.println("Connected to TCP: " + socket);
            } catch (IOException e) {
                if (retries >= 2) {
                    System.err.println("Connection failed, reattempts failed.");
                    Thread.currentThread().interrupt();
                    break;
                }
                System.err.println("Connection to TCP failed, retry in 5 seconds #" + ++retries);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public Package send(Package pkg) {
        while (true) {
            if (!isConnected) {
                connect();
            }
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                Encryptor encryptor = new DefaultEncryptor(null, null);
                byte[] arr = encryptor.encrypt(pkg);
                out.writeInt(arr.length);
                out.write(arr);
                out.flush();

                int responseLength = in.readInt();
                byte[] response = new byte[responseLength];
                in.readFully(response);

                DefaultDecryptor decryptor = new DefaultDecryptor(null, null);
                return decryptor.decodeIntoPackage(response);
            } catch (IOException e) {
                isConnected = false;
                System.err.println("Failed to send data, reconnecting");
            }
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}

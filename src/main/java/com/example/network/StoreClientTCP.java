package com.example.network;

import com.example.Message;
import com.example.Package;
import com.example.decryptor.Decryptor;
import com.example.decryptor.DefaultDecryptor;
import com.example.encryptor.DefaultEncryptor;
import com.example.encryptor.Encryptor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class StoreClientTCP {
    private Socket socket;
    boolean isConnected = false;

    public static void main(String[] args) {
        StoreClientTCP client = new StoreClientTCP();

        client.send(new Package((byte) 1, 500L, new Message(1, 23, "")));
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
                    System.err.println("Connection failed");
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

    public void send(Package pkg) {
        while (true) {
            if (!isConnected) {
                connect();
            }
            try {
                Encryptor encryptor = new DefaultEncryptor(null, null);
                byte[] arr = encryptor.encrypt(pkg);
                socket.getOutputStream().write(arr);
                socket.getOutputStream().flush();

                byte[] response = socket.getInputStream().readAllBytes();
                DefaultDecryptor decryptor = new DefaultDecryptor(null, null);
                Package responsePkg = decryptor.decodeIntoPackage(response);
                System.out.println("Received answer: " + responsePkg);
                break;
            } catch (IOException e) {
                System.err.println("Failed to send data, reconnecting");
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}

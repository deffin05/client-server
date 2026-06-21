package com.example.network;

import com.example.Message;
import com.example.Package;
import com.example.decryptor.DefaultDecryptor;
import com.example.encryptor.DefaultEncryptor;
import com.example.encryptor.Encryptor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class StoreClientUDP {
    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();

        byte[] buf = new byte[1024];
        InetAddress address = InetAddress.getLoopbackAddress();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, StoreServerUDP.PORT);
        clientSocket.send(packet);

        packet = new DatagramPacket(buf, buf.length);
        clientSocket.receive(packet);

        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Quote of the Moment: " + received);

        clientSocket.close();
    }
}

//public class StoreClientTCP {
//    private Socket socket;
//    boolean isConnected = false;
//
//    public static void main(String[] args) throws InterruptedException {
//        StoreClientTCP client = new StoreClientTCP();
//
//        client.send(new com.example.Package((byte) 1, 500L, new Message(1, 23, "")));
//        Thread.sleep(5000);
//        client.send(new com.example.Package((byte) 1, 500L, new Message(1, 23, "")));
//    }
//
//    public void connect() {
//        InetAddress addr = InetAddress.getLoopbackAddress();
//        int retries = 0;
//
//        while (!isConnected && !Thread.currentThread().isInterrupted()) {
//            try {
//                socket = new Socket(addr, StoreServerTCP.PORT);
//                isConnected = true;
//                System.out.println("Connected to TCP: " + socket);
//            } catch (IOException e) {
//                if (retries >= 2) {
//                    System.err.println("Connection failed");
//                    break;
//                }
//                System.err.println("Connection to TCP failed, retry in 5 seconds #" + ++retries);
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//    }
//
//    public void send(com.example.Package pkg) {
//        while (true) {
//            if (!isConnected) {
//                connect();
//            }
//            try {
//                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//                DataInputStream in = new DataInputStream(socket.getInputStream());
//                Encryptor encryptor = new DefaultEncryptor(null, null);
//                byte[] arr = encryptor.encrypt(pkg);
//                out.writeInt(arr.length);
//                out.write(arr);
//                out.flush();
//
//                int responseLength = in.readInt();
//                byte[] response = new byte[responseLength];
//                in.readFully(response);
//
//                DefaultDecryptor decryptor = new DefaultDecryptor(null, null);
//                Package responsePkg = decryptor.decodeIntoPackage(response);
//                System.out.println("Received answer: " + responsePkg);
//                break;
//            } catch (IOException e) {
//                isConnected = false;
//                System.err.println("Failed to send data, reconnecting");
//            } finally {
//                try {
//                    socket.close();
//                } catch (IOException ignored) {
//                }
//            }
//        }
//    }
//}

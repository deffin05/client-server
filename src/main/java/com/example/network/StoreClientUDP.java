package com.example.network;

import com.example.Message;
import com.example.Package;
import com.example.decryptor.DefaultDecryptor;
import com.example.encryptor.DefaultEncryptor;
import com.example.encryptor.Encryptor;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreClientUDP extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private static final AtomicInteger last_id = new AtomicInteger(1);
    private int id;

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            StoreClientUDP client = new StoreClientUDP();
            client.start();
        }
    }

    public StoreClientUDP() {
        try {
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(2000);
            this.address = InetAddress.getLoopbackAddress();
        } catch (SocketException e) {
            System.err.println("Socket initialization error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        id = last_id.incrementAndGet();
        for (int i = 0; i < 20; i++) {
            Package response = send(new Package((byte) 1, i, new Message(1, id, "")));
            System.out.println("Received response: " + response);
        }
        close();
    }

    public Package send(Package pkg) {
        Encryptor encryptor = new DefaultEncryptor(null, null);
        DefaultDecryptor decryptor = new DefaultDecryptor(null, null);

        int attempts = 0;
        while (attempts < 3) {
            try {
                byte[] pkgArr = encryptor.encrypt(pkg);
                DatagramPacket packet = new DatagramPacket(pkgArr, pkgArr.length, address, StoreServerUDP.PORT);
                socket.send(packet);

                byte[] buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                byte[] response = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength() + packet.getOffset());

                return decryptor.decodeIntoPackage(response);
            } catch (SocketTimeoutException e) {
                attempts++;
                if (attempts < 3){
                    System.out.println("UDP socket timeout, retrying #" + attempts);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("UDP request failed");
    }

    public void close() {
        socket.close();
    }
}

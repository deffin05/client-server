package com.example.network;

import com.example.NetworkPackage;
import com.example.receiver.Receiver;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class StoreServerUDP implements Receiver, Runnable {
    public static final int PORT = 13286;
    private final DatagramSocket socket;
    private byte[] buffer;
    private final BlockingQueue<NetworkPackage<byte[]>> outputQueue;

    public StoreServerUDP(BlockingQueue<NetworkPackage<byte[]>> outputQueue) {
        this.outputQueue = outputQueue;
        buffer = new byte[1024];
        try {
            this.socket = new DatagramSocket(PORT);
            System.out.println("UDP server started");
        } catch (SocketException e) {
            throw new RuntimeException("Failed to start UDP server");
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                try {
                    receiveMessage();
                } catch (SocketException e) {
                    System.out.println("UDP socket has been closed");
                } catch (IOException e) {
                    System.out.println("Error while reading/writing socket: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            socket.close();
        }
    }

    @Override
    public void receiveMessage() throws InterruptedException, IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        byte[] packetArr = Arrays.copyOfRange(buffer, packet.getOffset(), packet.getOffset() + packet.getLength());

        InetAddress address = packet.getAddress();
        int port = packet.getPort();

        NetworkPackage<byte[]> pkg = new NetworkPackage<>(packetArr, new UDPConnection(socket, address, port));
        outputQueue.put(pkg);
    }

    public void close() {
        socket.close();
    }
}

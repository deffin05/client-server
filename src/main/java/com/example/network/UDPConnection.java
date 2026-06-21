package com.example.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class UDPConnection implements Connection{
    private final InetAddress address;
    private final int port;
    private final DatagramSocket socket;

    public UDPConnection(DatagramSocket socket, InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.socket = socket;
    }

    @Override
    public void sendResponse(byte[] arr) throws IOException {
        DatagramPacket packet = new DatagramPacket(arr, arr.length, address, port);
        socket.send(packet);
    }
}

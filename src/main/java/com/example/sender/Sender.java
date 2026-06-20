package com.example.sender;

import java.net.InetAddress;

public interface Sender {
    void sendMessage(byte[] message);
}

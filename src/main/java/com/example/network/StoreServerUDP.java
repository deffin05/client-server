package com.example.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StoreServerUDP {
    public static final int PORT = 13286;

    public void startServer() throws IOException {
        try (ServerSocket s = new ServerSocket(PORT)) {
            System.out.println("TCP server started: " + s);
            while (true) {
                Socket socket = s.accept();
//                try {
//
//                }
            }
        }
    }
}

package com.example.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TCPConnection implements Connection{
    private final Socket socket;

    public TCPConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void sendResponse(byte[] arr) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(arr);
        out.flush();
    }
}

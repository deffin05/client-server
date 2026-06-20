package com.example.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPConnection implements Connection{
    private final Socket socket;

    public TCPConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void sendResponse(byte[] arr) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(arr.length);
        out.write(arr);
        out.flush();
    }
}

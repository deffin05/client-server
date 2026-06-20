package com.example.network;

import java.io.IOException;

public interface Connection {
    public void sendResponse(byte[] arr) throws IOException;
}

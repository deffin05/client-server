package com.example.receiver;

import java.io.IOException;

public interface Receiver {
    void receiveMessage() throws InterruptedException, IOException;
}

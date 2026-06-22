package com.example;

import com.example.database.Db;
import com.example.decryptor.DefaultDecryptor;
import com.example.encryptor.DefaultEncryptor;
import com.example.network.StoreServerTCP;
import com.example.network.StoreServerUDP;
import com.example.network.http.StoreServerHTTP;
import com.example.processor.DefaultProcessor;
import com.example.sender.DefaultSender;

import java.util.concurrent.*;

public class Pipeline {
    private static final int DECRYPTORS = 2;
    private static final int PROCESSORS = 1;
    private static final int ENCRYPTORS = 5;
    private static final int SENDERS = 2;

    private ExecutorService executor;
    private StoreServerTCP tcpServer;
    private StoreServerUDP udpServer;
    private Thread httpThread;

    public void startPipeline() {
        BlockingQueue<NetworkPackage<byte[]>> receiverQueue = new LinkedBlockingQueue<>(50);
        BlockingQueue<NetworkPackage<Package>> decryptorQueue = new LinkedBlockingQueue<>(50);
        BlockingQueue<NetworkPackage<Package>> processorQueue = new LinkedBlockingQueue<>(50);
        BlockingQueue<NetworkPackage<byte[]>> encryptorQueue = new LinkedBlockingQueue<>(50);

        Db database = new Db("warehouse.db");

        executor = Executors.newFixedThreadPool(2 + DECRYPTORS + PROCESSORS + ENCRYPTORS + SENDERS);

        tcpServer = new StoreServerTCP(receiverQueue);
        udpServer = new StoreServerUDP(receiverQueue);
        executor.submit(tcpServer);
        executor.submit(udpServer);

        StoreServerHTTP httpServer = new StoreServerHTTP(database);
        httpThread = new Thread(httpServer);
        httpThread.start();

        for (int i = 0; i < DECRYPTORS; i++) {
            executor.submit(new DefaultDecryptor(receiverQueue, decryptorQueue));
        }

        for (int i = 0; i < PROCESSORS; i++) {
            executor.submit(new DefaultProcessor(decryptorQueue, processorQueue, database));
        }

        for (int i = 0; i < ENCRYPTORS; i++) {
            executor.submit(new DefaultEncryptor(processorQueue, encryptorQueue));
        }

        for (int i = 0; i < SENDERS; i++) {
            executor.submit(new DefaultSender(encryptorQueue));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownPipeline));
    }

    public void shutdownPipeline() {
        executor.shutdown();
        tcpServer.close();
        udpServer.close();
        httpThread.interrupt();
        System.out.println("The pipeline has been shutdown");
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

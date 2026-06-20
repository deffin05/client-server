package com.example;

import com.example.decryptor.DefaultDecryptor;
import com.example.encryptor.DefaultEncryptor;
import com.example.processor.DefaultProcessor;
import com.example.receiver.FakeReceiver;
import com.example.sender.FakeSender;

import java.util.concurrent.*;

public class Pipeline {
    private static final int RECEIVERS = 2;
    private static final int DECRYPTORS = 2;
    private static final int PROCESSORS = 1;
    private static final int ENCRYPTORS = 5;
    private static final int SENDERS = 2;

    private ExecutorService executor;

    public static void main(String[] args) {
        Pipeline line = new Pipeline();
        line.startPipeline();
    }

    public void startPipeline() {
        BlockingQueue<byte[]> receiverQueue = new LinkedBlockingQueue<>(50);
        BlockingQueue<Package> decryptorQueue = new LinkedBlockingQueue<>(50);
        BlockingQueue<Package> processorQueue = new LinkedBlockingQueue<>(50);
        BlockingQueue<byte[]> encryptorQueue = new LinkedBlockingQueue<>(50);


        executor = Executors.newFixedThreadPool(RECEIVERS + DECRYPTORS + PROCESSORS + ENCRYPTORS + SENDERS);

        for (int i = 0; i < RECEIVERS; i++) {
            executor.submit(new FakeReceiver(receiverQueue));
        }

        for (int i = 0; i < DECRYPTORS; i++) {
            executor.submit(new DefaultDecryptor(receiverQueue, decryptorQueue));
        }

        for (int i = 0; i < PROCESSORS; i++) {
            executor.submit(new DefaultProcessor(decryptorQueue, processorQueue));
        }

        for (int i = 0; i < ENCRYPTORS; i++) {
            executor.submit(new DefaultEncryptor(processorQueue, encryptorQueue));
        }

        for (int i = 0; i < SENDERS; i++) {
            executor.submit(new FakeSender(encryptorQueue));
        }


        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownPipeline));
    }

    public void shutdownPipeline() {
        executor.shutdownNow();
        System.out.println("The pipeline has been shutdown");
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

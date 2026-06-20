package com.example.processor;

import com.example.Message;
import com.example.NetworkPackage;
import com.example.Package;
import com.example.network.Connection;

import java.util.concurrent.BlockingQueue;

public class DefaultProcessor implements Processor, Runnable {
    private final BlockingQueue<NetworkPackage<Package>> inputQueue;
    private final BlockingQueue<NetworkPackage<Package>> outputQueue;
    private Connection conn;

    public DefaultProcessor(BlockingQueue<NetworkPackage<Package>> inputQueue, BlockingQueue<NetworkPackage<Package>> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                NetworkPackage<Package> inputPkg = inputQueue.take();
                conn = inputPkg.getConnection();
                process(inputPkg.getData());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void process(Package pkg) throws InterruptedException {
        Package outputPkg = doProcess(pkg);
        outputQueue.put(new NetworkPackage<>(outputPkg, conn));
    }

    private Package doProcess(Package pkg) {
        String outputMessage = "";

        switch (pkg.getMessage().getcType()) {
            /*
             * 1 - Дізнатись кількість товару на складі
             * 2 - Списати певну кількість товару
             * 3 - Зарахувати певну кількість товару
             * 4 - Додати групу товарів
             * 5 - Додати назву товару до групи
             * 6 - Встановити ціну на конкретний товар
             * */
            case 1:
                outputMessage = "There are 10 products";
                break;
            case 2:
                outputMessage = "Products were removed";
                break;
            case 3:
                outputMessage = "Products were added";
                break;
            case 4:
                outputMessage = "Group of products was added";
                break;
            case 5:
                outputMessage = "Product was added to the group";
                break;
            case 6:
                outputMessage = "The price was set";
                break;
            default:
                outputMessage = "Unknown command";
        }
        return new Package(pkg.getbSrc(), pkg.getbPktId(),
                new Message(pkg.getMessage().getcType(), pkg.getMessage().getbUserId(), outputMessage));
    }
}

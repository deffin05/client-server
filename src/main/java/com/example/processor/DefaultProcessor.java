package com.example.processor;

import com.example.Message;
import com.example.Package;

import java.util.concurrent.BlockingQueue;

public class DefaultProcessor implements Processor, Runnable {
    private final BlockingQueue<Package> inputQueue;
    private final BlockingQueue<Package> outputQueue;

    public DefaultProcessor(BlockingQueue<Package> inputQueue, BlockingQueue<Package> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Package inputPkg = inputQueue.take();
                process(inputPkg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void process(Package pkg) throws InterruptedException {
        Package outputPkg = doProcess(pkg);
        outputQueue.put(outputPkg);
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

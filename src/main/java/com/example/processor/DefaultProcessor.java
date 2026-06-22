package com.example.processor;

import com.example.Message;
import com.example.NetworkPackage;
import com.example.Package;
import com.example.database.Db;
import com.example.database.Product;
import com.example.database.ProductFilters;
import com.example.network.Connection;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class DefaultProcessor implements Processor, Runnable {
    private final BlockingQueue<NetworkPackage<Package>> inputQueue;
    private final BlockingQueue<NetworkPackage<Package>> outputQueue;
    private final Db database;
    private Connection conn;

    public DefaultProcessor(BlockingQueue<NetworkPackage<Package>> inputQueue, BlockingQueue<NetworkPackage<Package>> outputQueue, Db database) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.database = database;
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
        String pkgMessage = pkg.getMessage().getMessage();

        switch (pkg.getMessage().getcType()) {
            /*
             * 1 - Отримати всі товари
             * 2 - Отримати товар за ID
             * 3 - Створити товар
             * 4 - Оновити товар
             * 5 - Видалити товар
             * */
            // Повідомлення у форматі id=1,name=сир,...
            case 1:
                StringBuilder messageBuilder = new StringBuilder();
                ProductFilters filterObject = getProductFilters(pkgMessage);

                List<Product> products = database.getAll(filterObject);
                for (Product product : products) {
                    messageBuilder.append(product);
                    messageBuilder.append(",");
                }
                if (!messageBuilder.isEmpty()) {
                    messageBuilder.deleteCharAt(messageBuilder.length() - 1);
                }
                outputMessage = messageBuilder.toString();
                break;
            case 2:
                String idStr = pkgMessage.substring(3);
                int id = Integer.parseInt(idStr);
                Optional<Product> productOptional = database.getById(id);
                Product fetchedProduct = productOptional.orElse(null);
                if (fetchedProduct != null) outputMessage = fetchedProduct.toString();
                break;
            case 3:
                Product createdProduct = getProductParameters(pkgMessage);
                database.insert(createdProduct);
                outputMessage = "Product was created";
                break;
            case 4:
                Product updatedProduct = getProductParameters(pkgMessage);
                if (database.update(updatedProduct)) {
                    outputMessage = "Product was updated";
                } else {
                    outputMessage = "Failed to update products";
                }
                break;
            case 5:
                String idDeleteStr = pkgMessage.substring(3);
                int idDelete = Integer.parseInt(idDeleteStr);
                if (database.delete(idDelete)) {
                    outputMessage = "Product was deleted";
                } else {
                    outputMessage = "Failed to delete product";
                }
                break;
            default:
                outputMessage = "Unknown command";
        }
        return new Package(pkg.getbSrc(), pkg.getbPktId(),
                new Message(pkg.getMessage().getcType(), pkg.getMessage().getbUserId(), outputMessage));
    }

    private static ProductFilters getProductFilters(String pkgMessage) {
        String[] filters = pkgMessage.split(",");
        ProductFilters filterObject = new ProductFilters();
        for (String filter : filters) {
            String[] pair = filter.split("=");
            if (pair.length != 2) continue;
            switch (pair[0]) {
                case "name":
                    filterObject.setName(pair[1]);
                    break;
                case "remainderFrom":
                    filterObject.setRemainderFrom(Integer.parseInt(pair[1]));
                    break;
                case "remainderTo":
                    filterObject.setRemainderTo(Integer.parseInt(pair[1]));
                    break;
                case "priceFrom":
                    filterObject.setPriceFrom(Double.parseDouble(pair[1]));
                    break;
                case "priceTo":
                    filterObject.setPriceTo(Double.parseDouble(pair[1]));
                    break;
                case "category":
                    filterObject.setCategory(pair[1]);
                    break;
                case "page":
                    filterObject.setPage(Integer.parseInt(pair[1]));
                    break;
            }
        }
        return filterObject;
    }

    private static Product getProductParameters(String pkgMessage) {
        String[] params = pkgMessage.split(",");
        String name = null;
        Integer remainder = null;
        Double price = null;
        String category = null;

        for (String parameter : params) {
            String[] pair = parameter.split("=");
            if (pair.length != 2) break;
            switch (pair[0]) {
                case "name":
                    name = pair[1];
                    break;
                case "remainder":
                    remainder = Integer.parseInt(pair[1]);
                    break;
                case "price":
                    price = Double.parseDouble(pair[1]);
                    break;
                case "category":
                    category = pair[1];
                    break;
            }
        }

        if (name == null || remainder == null || price == null || category == null)
            throw new RuntimeException("Invalid product received.");
        return new Product(name, remainder, price, category);
    }
}

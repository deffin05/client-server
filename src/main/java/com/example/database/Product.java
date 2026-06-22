package com.example.database;

public class Product {
    private Integer id;
    private String name;
    private int remainder;
    private double price;
    private String category;

    public Product(String name, int remainder, double price, String category) {
        this(null, name, remainder, price, category);
    }

    public Product(Integer id, String name, int remainder, double price, String category) {
        this.id = id;
        this.name = name;
        this.remainder = remainder;
        this.price = price;
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRemainder() {
        return remainder;
    }

    public void setRemainder(int remainder) {
        this.remainder = remainder;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", remainder=" + remainder +
                ", price=" + price +
                ", category='" + category + '\'' +
                '}';
    }
}

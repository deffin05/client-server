package com.example.database;

public class ProductFilters {
    private String name;
    private Integer remainderFrom;
    private Integer remainderTo;
    private Double priceFrom;
    private Double priceTo;
    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRemainderFrom() {
        return remainderFrom;
    }

    public void setRemainderFrom(Integer remainderFrom) {
        this.remainderFrom = remainderFrom;
    }

    public Integer getRemainderTo() {
        return remainderTo;
    }

    public void setRemainderTo(Integer remainderTo) {
        this.remainderTo = remainderTo;
    }

    public Double getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(Double priceFrom) {
        this.priceFrom = priceFrom;
    }

    public Double getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(Double priceTo) {
        this.priceTo = priceTo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

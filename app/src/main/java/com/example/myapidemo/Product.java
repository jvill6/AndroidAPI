package com.example.myapidemo;
public class Product {

    private int id;
    private String sku;
    private String name;
    private String description;
    private String category;
    private String price;
    private int stock_quantity;
    private int is_active;
    private String created_at;
    private String updated_at;

    public Product() {
    }

    public Product(
            String sku,
            String name,
            String description,
            String category,
            String price,
            int stock_quantity,
            int is_active) {

        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock_quantity = stock_quantity;
        this.is_active = is_active;
    }

    public int getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public int getStock_quantity() {
        return stock_quantity;
    }

    public int getIs_active() {
        return is_active;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setStock_quantity(int stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}

package com.example.assignment3;

import java.io.Serializable;

public class CoffeeItem implements Serializable {
    //name, description, price, sizes
    private String name;
    private String description;
    private double price;
    private int imageID;
    private String sizes;

    public CoffeeItem(){
        this.name = "";
        this.description = "";
        this.price = 0.0;
        this.sizes = "";
        this.imageID = 0;
    }

    public CoffeeItem(String name, double price, String sizes, String description, int imageID){
        this.name = name;
        this.price = price;
        this.sizes = sizes;
        this.description = description;
        this.imageID = imageID;
    }

    public String getName() {return name;}
    public String getSizes() {return sizes;}
    public String getDescription() {return description;}
    public double getPrice() {return price;}
    public int getImageID() {
        return imageID;
    }

}


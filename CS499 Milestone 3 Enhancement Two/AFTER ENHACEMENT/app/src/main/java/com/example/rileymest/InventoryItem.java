package com.example.rileymest;

public class InventoryItem {

    public long id;
    public String name;
    public int qty;
    public InventoryItem(long id, String name, int qty) {
        this.id = id;
        this.name = name;
        this.qty = qty;
    }
}

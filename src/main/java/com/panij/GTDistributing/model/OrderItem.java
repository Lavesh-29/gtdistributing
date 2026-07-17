package com.panij.GTDistributing.model;

import lombok.Data;

@Data
public class OrderItem {
    private String item_Ref;
    private String description;
    private int qty;

 }

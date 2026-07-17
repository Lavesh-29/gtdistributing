package com.panij.GTDistributing.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "item_ref")
    private String item_Ref;
    private String description;
    private String category;
    private String subcategory;
    private Double sell_Unit_Price; // Keeping the field name as is
    private String upc;  // UPC stored as String
    private String image_Path; // Keeping the field name as is
    private Integer quantity = 1;
    private Integer isNew  = 0;
    private Integer qty_limit =100; // or int qtyLimit;
   }

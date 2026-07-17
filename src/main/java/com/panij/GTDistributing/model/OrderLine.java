package com.panij.GTDistributing.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "order_items")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String itemRef;

    private String description;

    private Integer qty;

    private Integer pickedQty = 0;

    // getters/setters
}
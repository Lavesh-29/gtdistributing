package com.panij.GTDistributing.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "orders")
public class OrderHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerNo;
    private String customerName;
    private String email;

    private LocalDateTime orderDate;

    private BigDecimal totalPrice;

    private String status;

    private String assignedEmployee;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    // getters/setters
}
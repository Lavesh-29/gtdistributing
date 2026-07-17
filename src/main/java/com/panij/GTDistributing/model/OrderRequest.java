package com.panij.GTDistributing.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data
public class OrderRequest {
    private String customerName;
    private String emailId;
    private String accountNo;
    private String password;
    private String emailBody;
    private List<OrderItem> items;
    private double totalPrice;
    private String totalQuantity;

}

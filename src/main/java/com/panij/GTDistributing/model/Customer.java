package com.panij.GTDistributing.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@Getter
@Setter
public class Customer {

    @Id
    private Integer custNo;
    private String name;
    private String name2;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String county;
    private String zip;
    private String phone;
    private String altPhone;
    private String password;


    public String getCustomerName() {
        return this.name;
      }
}
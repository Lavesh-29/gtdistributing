package com.panij.GTDistributing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationForm {
    private String legalName;
    private String dba;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String license;
    private String taxId;
    private String salesTax;
    private String businessType;
    private String contactPerson;
    private String title;
    private String businessPhone;
    private String cellPhone;
    private String fax;
    private String email;
    private String paymentMethod;
    private String bankName;
    private String bankPhone;
    private String bankLocation;
    private String bankContact;
    private String accountNumber;
    private String routingNumber;
    private String signatureDataUrl;

}

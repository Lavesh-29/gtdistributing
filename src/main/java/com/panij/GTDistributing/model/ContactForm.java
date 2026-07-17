package com.panij.GTDistributing.model;

import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactForm {
    private String firstName;
    private String middleName;
    private String lastName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zipcode;
    private String daytimePhone;
    private String eveningPhone;
    private String email;
    private String comments;
}

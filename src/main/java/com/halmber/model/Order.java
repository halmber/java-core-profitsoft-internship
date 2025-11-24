package com.halmber.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private String id;
    private Customer customer;
    private String status;   // NEW, PROCESSING, DONE, CANCELED
    private String tags; // "urgent, gift, newCustomer"
    private String paymentMethod; // "card", "cash", "PayPal"
    private double amount;
    private long createdAt;
}

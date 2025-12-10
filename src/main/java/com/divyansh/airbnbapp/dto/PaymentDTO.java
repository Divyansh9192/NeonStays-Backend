package com.divyansh.airbnbapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private String amount;
    private String orderId;
    private String email;
    private String date;
    private String last4;
}

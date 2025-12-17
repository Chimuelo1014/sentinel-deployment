package com.sentinel.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {
    private String initPoint; // URL to redirect (if using standard checkout)
    private String preferenceId; // ID for the modal/SDK
}

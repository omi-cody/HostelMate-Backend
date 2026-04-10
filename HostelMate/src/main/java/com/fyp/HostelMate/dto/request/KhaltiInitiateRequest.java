package com.fyp.HostelMate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the payload we send TO Khalti's server when initiating a payment.
// Khalti returns a payment_url and pidx that the frontend uses to open Khalti's checkout.
// Amount is in PAISA (1 Rs = 100 paisa) - so Rs 500 = 50000 paisa.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhaltiInitiateRequest {

    private String return_url;      // where Khalti redirects after payment (your frontend URL)
    private String website_url;     // your website's base URL
    private Long amount;            // amount in PAISA
    private String purchase_order_id;   // your internal payment UUID
    private String purchase_order_name; // human-readable label shown on Khalti checkout
}

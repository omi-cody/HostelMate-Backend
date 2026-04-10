package com.fyp.HostelMate.dto.response;

import lombok.Data;

// Khalti's response when we call their /epayment/initiate/ API.
// We return the payment_url to the frontend so it can redirect the student to Khalti.
@Data
public class KhaltiResponse {
    private String pidx;          // Khalti's unique payment identifier - store this
    private String payment_url;   // open this URL to show Khalti payment page
    private String expires_at;
    private String expires_in;
}

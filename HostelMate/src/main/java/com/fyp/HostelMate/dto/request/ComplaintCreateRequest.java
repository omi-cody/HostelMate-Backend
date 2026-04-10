package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.ComplaintType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Student submits this to report a complaint or request maintenance at their hostel.
// Only admitted students can submit requests.
@Data
public class ComplaintCreateRequest {

    @NotNull(message = "Request type is required")
    private ComplaintType requestType; // COMPLAINT or MAINTENANCE

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;
}

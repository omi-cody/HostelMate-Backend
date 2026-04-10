package com.fyp.HostelMate.dto.request;

import lombok.Data;

// Hostel sends this to reject an application or cancel after a visit.
// The remark is optional but helps the student understand why.
@Data
public class RejectApplicationRequest {
    private String remark;
}

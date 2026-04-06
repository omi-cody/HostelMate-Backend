package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Admission;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AdmissionResponse {
    private UUID admissionId;
    private UUID studentId;
    private String studentName;
    private UUID hostelId;
    private String hostelName;
    private UUID roomId;
    private String roomNumber;
    private String roomType;
    private LocalDate admissionDate;
    private Double monthlyFee;
    private LocalDate nextPaymentDue;
    private String mealPreference;
    private Boolean isActive;
    private LocalDate leaveDate;

    public static AdmissionResponse from(Admission a) {
        return AdmissionResponse.builder()
                .admissionId(a.getAdmissionId())
                .studentId(a.getStudent().getStudentId())
                .studentName(a.getStudent().getFullName())
                .hostelId(a.getHostel().getHostelId())
                .hostelName(a.getHostel().getHostelName())
                .roomId(a.getRoom().getRoomId())
                .roomNumber(a.getRoom().getRoomNumber())
                .roomType(a.getRoom().getRoomType().name())
                .admissionDate(a.getAdmissionDate())
                .monthlyFee(a.getMonthlyFee())
                .nextPaymentDue(a.getNextPaymentDue())
                .mealPreference(a.getMealPreference().name())
                .isActive(a.getIsActive())
                .leaveDate(a.getLeaveDate())
                .build();
    }
}

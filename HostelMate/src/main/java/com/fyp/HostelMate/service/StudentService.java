package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Student;
import java.util.UUID;

public interface StudentService {
    Student getStudentProfile(UUID studentId);
    Student updateStudentProfile(UUID studentId, Student studentDetails);
    
    void submitKyc(UUID studentId, com.fyp.HostelMate.dto.request.StudentKycRequest request) throws java.io.IOException;
}

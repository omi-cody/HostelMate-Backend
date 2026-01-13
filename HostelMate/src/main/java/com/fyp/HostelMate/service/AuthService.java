package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.response.AuthResponse;
import com.fyp.HostelMate.dto.request.HostelRegistrationRequest;
import com.fyp.HostelMate.dto.request.LoginRequest;
import com.fyp.HostelMate.dto.request.StudentRegistrationRequest;

public interface AuthService {

    public void registerStudent(StudentRegistrationRequest studentRegistrationRequest);
    public void registerHostel(HostelRegistrationRequest hostelRegistrationRequest);
    public AuthResponse login (LoginRequest loginRequest);
}

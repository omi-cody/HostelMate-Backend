package com.fyp.HostelMate.exceptions;

// Thrown when a valid request violates a business rule.
// Examples: trying to delete a room that has students, applying while KYC is pending.
// Results in a 400 response with a descriptive message.
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

package com.fyp.HostelMate.exceptions;

// Thrown when a requested entity (student, hostel, room, etc.) doesn't exist in the database.
// Results in a 404 response to the client.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

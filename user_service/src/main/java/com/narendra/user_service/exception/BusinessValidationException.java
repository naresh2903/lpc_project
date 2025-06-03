package com.narendra.user_service.exception;

public class BusinessValidationException extends Exception {

    // Default constructor
    public BusinessValidationException() {
        super();
    }

    // Constructor with error message
    public BusinessValidationException(String message) {
        super(message);
    }

    // Constructor with error message and cause
    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with cause
    public BusinessValidationException(Throwable cause) {
        super(cause);
    }
}

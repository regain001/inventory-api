package com.ecom.inventory.exception;

public class ResourceNotFoundException extends Exception {

    String resourceId;
    String message;

    public ResourceNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public ResourceNotFoundException(Long resourceId, String message) {
        super(message);
        this.resourceId = resourceId.toString();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
package com.ecom.inventory.exception;

import java.util.LinkedHashMap;

public class CustomException extends RuntimeException{
    String message;
    private LinkedHashMap<String, String> messages = new LinkedHashMap();
    private String messageSeparator = "\n";
    private String DEFAULT_BASE_MESSAGE = "Data validation failed";
    private Boolean isErrorMessageAdded = false;
    
    public LinkedHashMap<String, String> getMessages() {
        return messages;
    }
    public CustomException() {
        messages.put("default", DEFAULT_BASE_MESSAGE);
    }

    public CustomException(String message) {
        super(message);
        this.message = message;
    }
    public CustomException(String key, String baseMessage) {
        messages.put(key, baseMessage);
    }

    public void addErrorMessage(String key, String newMessage) {
        messages.remove("default");
        messages.put(key, newMessage);
        isErrorMessageAdded = true;
    }
    

    public void setMessageSeparator(String messageSeparator) {
        this.messageSeparator = messageSeparator;
    }

    public Boolean isValidationErrorOccured() {
        return isErrorMessageAdded;
    }

    @Override
    public String getMessage() {
        if (messages.size() == 1) {
            return messages.keySet().toArray()[0].toString()+": "+ messages.values().toArray()[0].toString();
        }
        if(messages.isEmpty()) return message;
        StringBuilder sb = new StringBuilder();
        boolean shouldAppendSeparator = false;
        for (String key : messages.keySet()) {
            if (shouldAppendSeparator) {
               sb.append(messageSeparator);
            }
            sb.append(key);
            sb.append(": ");
            sb.append(messages.get(key));
            shouldAppendSeparator = true;
        }
        return sb.toString();
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.exception;

import java.util.LinkedHashMap;

/**
 *
 * @author SohanBappy
 */
public class UserInputValidationException extends Exception {
    private LinkedHashMap<String, String> messages = new LinkedHashMap();
    private String messageSeparator = "\n";
    private String DEFAULT_BASE_MESSAGE = "Data validation failed";
    private Boolean isErrorMessageAdded = false;

    public LinkedHashMap<String, String> getMessages() {
        return messages;
    }
    
    public UserInputValidationException() {
        messages.put("default", DEFAULT_BASE_MESSAGE);
    }
    
    public UserInputValidationException(String message) {
        messages.put("default", message);
        isErrorMessageAdded = true;
    }

    public UserInputValidationException(String key, String baseMessage) {
        super(baseMessage);
        messages.put(key, baseMessage);
    }

    public void addErrorMessage(String key, String newMessage) {
        messages.remove("default");
        messages.put(""+(messages.size()+1)+". "+key, newMessage);
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
            return messages.values().toArray()[0].toString();
        }
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.dto.common;

import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.security.AppConstants;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;

/**
 *
 * @author SohanBappy
 */
public class ResponseDto {
    private Boolean success = false;
    private Integer status;
    private String message;
    private Object data;
    private LinkedHashMap<String, String> errors;
    private String debugHint;

    public ResponseDto() {
    }

    public ResponseDto(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public void setSuccess(){
        if(message == null){
            this.message = "Success";  
        }
        this.success = true;
        this.status = HttpStatus.OK.value();
    }
    
    public void setSuccess(String message){
        this.success = true;
        this.message = message;
        this.status = HttpStatus.OK.value();
    }
    
    public void setError(){
        if(this.message==null){
            setError("Failed");
        }
        else {
            this.success = false;
        }
    }
    
    public void setError(Throwable e){
        
        if(e instanceof UserInputValidationException){
            UserInputValidationException ex = (UserInputValidationException) e;
            setError("Invalid Input");
            errors = new LinkedHashMap<>();
            for (String key: ex.getMessages().keySet()) {
                errors.put(key, ex.getMessages().get(key));
            }
        }
        if(e.getMessage() != null){
            setError("Failed: "+e.getMessage());
        } else{
            setError("Failed");
        }
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if(AppConstants.isDevMode != null && AppConstants.isDevMode){
            String hint = e.getClass().getSimpleName()+": "+e.getMessage();
            this.debugHint = hint;
        }
    }
    
    public void setError(String message){
        this.success = false;
        this.message = message;
        if(this.errors == null){
            this.errors = new LinkedHashMap<>();
        }
    }
    
    public void addError(String message){
        if(this.message == null){
            this.message = message;
        }
        this.success = false;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
        setSuccess();
    }

    public LinkedHashMap<String, String> getErrors() {
        return errors;
    }

    public void setErrors(LinkedHashMap<String, String> errors) {
        this.errors = errors;
    }

    public String getDebugHint() {
        return debugHint;
    }

    public void setDebugHint(String debugHint) {
        this.debugHint = debugHint;
    }
    
    
}

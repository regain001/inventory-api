/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.exception;

/**
 *
 * @author Fahim
 */
public class WebCallException extends Exception {

    public WebCallException() {
    }

    public WebCallException(String message) {
        super(message);
    }

    public WebCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebCallException(Throwable cause) {
        super(cause);
    }

    public WebCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}

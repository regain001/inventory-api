/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.util.db;

/**
 *
 * @author SohanBappy
 */
public class PojoTransformerException extends Exception{

    public PojoTransformerException() {
    }

    public PojoTransformerException(String message) {
        super(message);
    }

    public PojoTransformerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PojoTransformerException(Throwable cause) {
        super(cause);
    }

    public PojoTransformerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}

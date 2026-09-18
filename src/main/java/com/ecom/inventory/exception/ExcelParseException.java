/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.exception;

/**
 * @author Fahim
 */
public class ExcelParseException extends Exception {

    public ExcelParseException() {
    }

    public ExcelParseException(String message) {
        super(message);
    }

    public ExcelParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelParseException(Throwable cause) {
        super(cause);
    }

    public ExcelParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

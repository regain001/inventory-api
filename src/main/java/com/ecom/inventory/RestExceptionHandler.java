/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory;

import com.ecom.inventory.dto.common.ResponseDto;
import com.ecom.inventory.exception.CustomException;
import com.ecom.inventory.exception.ExcelParseException;
import com.ecom.inventory.exception.UserInputValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * @author Administrator
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);
    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String message, Throwable ex) {
        ResponseDto ret = new ResponseDto();
        ret.setError(ex);
        ret.setStatus(status.value());
        if(message!=null){
            ret.setMessage(message);
        }
        
        return new ResponseEntity<Object>(ret, status);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllUnhandledExceptions(Exception ex){
        log.error(ex.getMessage());
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Server internal error", ex);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handlePermissionException(BadCredentialsException ex){
        String message = "Bad credentials";
        if(ex.getMessage()!=null){
            message = ex.getMessage();
        }
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, message, ex);
    }
    
    @ExceptionHandler(UserInputValidationException.class)
    public ResponseEntity<?> handleValidationException(UserInputValidationException ex){
        //ex.printStackTrace();
        System.out.println(ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }
    
//    @ExceptionHandler(KohaServiceAccessException.class)
//    public ResponseEntity<?> handleValidationException(KohaServiceAccessException ex){
//        //ex.printStackTrace();
//        System.out.println(ex.getMessage());
//        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
//    }
    
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleValidationException(UsernameNotFoundException ex){
        //ex.printStackTrace();
        System.out.println(ex.getMessage());
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }
    
    @ExceptionHandler(ExcelParseException.class)
    public ResponseEntity<?> handleValidationException(ExcelParseException ex){
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException ex){
        //ex.printStackTrace();
        log.warn(ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {
        ResponseDto ret = new ResponseDto();
        ret.setMessage("Access denied");
        ret.setStatus(HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(ret, HttpStatus.FORBIDDEN);
    }
    
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<?> handleBadRequestException(HttpMessageNotReadableException ex){
//        ex.printStackTrace();
//        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
//    }
    
}

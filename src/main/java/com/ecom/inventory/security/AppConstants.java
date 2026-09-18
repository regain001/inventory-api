/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * @author SohanBappy
 */
@Component
@PropertySource("classpath:application.properties")
public class AppConstants {
    @Value("${comm.isDevMode}")
    public static Boolean isDevMode = true; //need to try to get from application properties file
    
    public static final String APP_TYPE_EMS="EMS";

}

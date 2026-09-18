/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.util.common;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Fahim
 */
public class PropertiesLoader {

    private static Properties configuration = new Properties();
    private static Boolean isLoaded = false;
    
    private static void loadProperties() {
        if(!isLoaded){
            configuration = new Properties();
            try {
                InputStream inputStream = PropertiesLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties");
              configuration.load(inputStream);
              inputStream.close();
              isLoaded = true;
            } catch (Exception e) {
            }
        }
    }
    
    public static String readProperty(String propertyName){
        loadProperties();
        return configuration.getProperty(propertyName);
    }
}

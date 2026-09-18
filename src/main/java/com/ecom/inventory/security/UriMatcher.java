/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.security;

/**
 *
 * @author SohanBappy
 */
public class UriMatcher {

    private static String[] processedWhiteList;
    private static boolean isProcessed = false;
    
    public static boolean matched(String targetUrl, String[] matchingList) {
        if(targetUrl == null){
            return false;
        }
        for (String matcher : matchingList) {
            String processed = matcher;
            processed = processed.replaceAll("\\*",".*");
            if(targetUrl.matches(processed)){
                return true;
            }
        }
        return false;
    }
    
    public static boolean matchedAuthWhiteList(String targetUri) {
        if(targetUri == null){
            return false;
        }
        preProcessAuthWhiteList();
        
        for (String matcher : processedWhiteList) {
            if(targetUri.matches(matcher)){
                return true;
            }
        }
        return false;
    }
    
    private static void preProcessAuthWhiteList(){
        if(!isProcessed){
            processedWhiteList = new String[SecurityConstants.AUTH_WHITELIST.length];
            for(int i = 0; i < SecurityConstants.AUTH_WHITELIST.length; i++){
                String processed = SecurityConstants.AUTH_WHITELIST[i];
                processed = processed.replaceAll("\\*",".*");
                processedWhiteList[i] = processed;
            }
            isProcessed = true;
        }
    }
}

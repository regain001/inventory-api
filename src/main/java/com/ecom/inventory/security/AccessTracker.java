/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.security;

import com.ecom.inventory.util.common.PropertiesLoader;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Fahim
 */
public class AccessTracker {
    public static final Long DEFAULT_INACTIVITY_THRESHOLD_MILLI = 3600 * 1000L;
    public static Long inactivityThresholdMilli = DEFAULT_INACTIVITY_THRESHOLD_MILLI;
    
    private static Map<String, Date> refreshTokenAccessMap = new ConcurrentHashMap<String, Date>();
    
    static{
         String val = PropertiesLoader.readProperty("token.admin.inactivitythresholdMilli");
         String devModeVal = PropertiesLoader.readProperty("comm.isDevMode");
         try {
            inactivityThresholdMilli = Long.parseLong(val);
            if(devModeVal!=null && devModeVal.equalsIgnoreCase("true")){    //dev server increase inactivity threshold
                inactivityThresholdMilli = 60*inactivityThresholdMilli;
            }
            
        } catch (Exception e) {
        }
    }
    
    public static void accessed(String sessionId){
        if(sessionId!=null){
            Date currentTime = new Date();
            System.out.println("["+currentTime+"] SessionID: "+sessionId+", SessionStorage size: "+refreshTokenAccessMap.size());
            refreshTokenAccessMap.put(sessionId, currentTime);
        }
    }
    
    public static Boolean isAllowed(String sessionId){
        if(sessionId==null){
            System.out.println("Denying access with null session.");
            return false;
        }
        System.out.println("\tChecking last access time for: "+sessionId);
        try {
            Date currentTime = new Date();
            Date lastAccessTime = refreshTokenAccessMap.get(sessionId);
            System.out.println("\tLast access time: "+lastAccessTime);
            if(lastAccessTime==null){
                return false;
            }
            Long elapsedTime = currentTime.getTime() - lastAccessTime.getTime();
            System.out.println("\tElapsed time: "+elapsedTime+", Threshold: "+inactivityThresholdMilli);
            if(elapsedTime<-1){ //clock error
                System.out.println("\t!!!CLOCK ERROR: last access time is behind current time.");
                refreshTokenAccessMap.remove(sessionId);
                return false;
            }
            else if(elapsedTime > inactivityThresholdMilli){
                System.out.println("\t -- Removing session ID from tracking for inactivity --");
                refreshTokenAccessMap.remove(sessionId);
                return false;
            }
            else {
                System.out.println("\tSession is active");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\t!!!Session is Inactive: no tracking information found");
        return false;
    }
}

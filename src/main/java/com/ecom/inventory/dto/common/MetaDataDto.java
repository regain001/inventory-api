/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.dto.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author SohanBappy
 */
public class MetaDataDto {
    
    private LinkedHashMap<String, List<Tuple>> metaData;
    
    
    public void addValue(String key, String label, Object value){
        if(metaData == null){
            metaData = new LinkedHashMap<>();
        }
        List<Tuple> tuples = metaData.get(key);
        if(tuples == null){
            tuples = new ArrayList<Tuple>();
            metaData.put(key, tuples);
        }
        tuples.add(new Tuple(label,value));
    }

    public void addNullList(String key){
        if(metaData==null){
            metaData = new LinkedHashMap<>();
        }
        metaData.put(key, new ArrayList<>());
    }
    
    public Boolean isExist(String key){
        if(metaData!=null){
            return metaData.containsKey(key);
        }
        return false;
    }
    
    
    
    public LinkedHashMap<String, List<Tuple>> getMetaData() {
        return metaData;
    }

    public void setMetaData(LinkedHashMap<String, List<Tuple>> metaData) {
        this.metaData = metaData;
    }

}

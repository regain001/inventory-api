/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.entity.enums;

import com.ecom.inventory.dto.common.MetaDataDto;

import java.util.List;

/**
 *
 * @author Fahim
 */
public class MetaMaker {
    public static <E extends MetaProvider> void addEntityObjects(MetaDataDto targetMeta, String key, List<E> entityObjects){
        if(targetMeta!=null && key!=null && entityObjects!=null){
            for(MetaProvider obj: entityObjects){
                targetMeta.addValue(key, obj.getLabel(), obj.getValue());
            }
        }
    }
}

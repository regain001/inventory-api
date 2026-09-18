/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.dto.common;

/**
 *
 * @author SohanBappy
 */
public class Tuple {
    
    private String label;
    private Object value;

    public Tuple() {}

    public Tuple(String label) {
        this.label = label;
    }

    public Tuple(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

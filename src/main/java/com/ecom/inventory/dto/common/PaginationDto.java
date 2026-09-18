/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.dto.common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fahim
 */
public class PaginationDto {
    private Integer totalRecords = 0;
    private Integer fetchedRecords = 0;
    private Integer start = 0;
    private Integer limit = 0;
    private List records = new ArrayList<>();

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getFetchedRecords() {
        return fetchedRecords;
    }

    public void setFetchedRecords(Integer fetchedRecords) {
        this.fetchedRecords = fetchedRecords;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public List getRecords() {
        return records;
    }

    public void setRecords(List records) {
        this.records = records;
    }
    
    
}

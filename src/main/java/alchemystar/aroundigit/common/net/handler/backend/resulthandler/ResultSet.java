/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.resulthandler;

import java.util.ArrayList;
import java.util.List;

/**
 * ResultSet
 * @Author lizhuyang
 */
public class ResultSet {

    private int fieldCount;
    private List<String> fields;
    // Just For the String value
    private List<String[]> rows;

    public ResultSet() {
        fieldCount = 0 ;
        fields = new ArrayList<String>();
        rows = new ArrayList<String[]>();
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public void setRows(List<String[]> rows) {
        this.rows = rows;
    }

    public void addField(String field){
        fields.add(field);
    }

    public void addRow(String[] row){
        rows.add(row);
    }

    public void clear(){
        fields.clear();
        rows.clear();
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    @Override
    public String toString() {
        return "ResultSet{" +
                "fieldCount=" + fieldCount +
                ", fields=" + fields +
                ", rows=" + rows +
                '}';
    }
}

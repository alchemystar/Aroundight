/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.loader;

import java.util.Arrays;

/**
 * @Author lizhuyang
 */
public class TableRuleConfig {

    private final String[] columns;
    private final String tableName;
    private final String dbRender;
    private final String tbRender;

    public TableRuleConfig(String[] columns, String tableName, String dbRender, String tbRender) {
        this.columns = columns;
        this.tableName = tableName;
        this.dbRender = dbRender;
        this.tbRender = tbRender;
    }

    public boolean contains(String column){
        for(int i=0;i<columns.length;i++){
            if(columns[i].equals(column)){
                return true;
            }
        }
        return false;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDbRender() {
        return dbRender;
    }

    public String getTbRender() {
        return tbRender;
    }

    @Override
    public String toString() {
        return "TableRuleConfig{" +
                "columns=" + Arrays.toString(columns) +
                ", tableName='" + tableName + '\'' +
                ", dbRender='" + dbRender + '\'' +
                ", tbRender='" + tbRender + '\'' +
                '}';
    }
}

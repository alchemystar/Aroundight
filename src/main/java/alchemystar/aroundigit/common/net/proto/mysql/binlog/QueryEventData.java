/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * @Author lizhuyang
 */
public class QueryEventData implements EventData {

    private long threadId;
    private long executionTime;
    private int errorCode;
    private byte[] statusVar;
    private String database;
    private String sql;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        threadId = mm.readUB4();
        executionTime = mm.readUB4();
        mm.move(1); // length of the name of the database
        errorCode = mm.readUB2();
        int statusVarLength = mm.readUB2(); // status variables block
        statusVar = mm.readBytes(statusVarLength);
        database = mm.readStringWithNull();
        sql = mm.readString();
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("QueryEventData");
        sb.append("{threadId=").append(threadId);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", database='").append(database).append('\'');
        sb.append(", sql='").append(sql).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

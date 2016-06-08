/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend;

import alchemystar.aroundigit.common.net.handler.backend.resulthandler.ChecksumType;
import alchemystar.aroundigit.common.net.handler.backend.resulthandler.ResultSet;

/**
 * @Author lizhuyang
 */
public class BinlogContext {

    private String binLogFileName;
    private Long binlogPosition;
    private String executedGtidSet;
    private GtidSet gtidSet;
    private ChecksumType checksumType;

    public void handlePositionResult(ResultSet resultSet){
        String[] row = resultSet.getRows().get(0);
        binLogFileName = row[0];
        binlogPosition = Long.parseLong(row[1]);
        executedGtidSet = row[4].replace("\n","");
        checksumType = ChecksumType.NONE;
    }

    public void useGtidSet(){
        gtidSet = new GtidSet(executedGtidSet);
    }

    public String getBinLogFileName() {
        return binLogFileName;
    }

    public void setBinLogFileName(String binLogFileName) {
        this.binLogFileName = binLogFileName;
    }

    public Long getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(Long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public String getExecutedGtidSet() {
        return executedGtidSet;
    }

    public void setExecutedGtidSet(String executedGtidSet) {
        this.executedGtidSet = executedGtidSet;
    }

    public GtidSet getGtidSet() {
        return gtidSet;
    }

    public void setGtidSet(GtidSet gtidSet) {
        this.gtidSet = gtidSet;
    }

    public ChecksumType getChecksumType() {
        return checksumType;
    }

    public void setChecksumType(ChecksumType checksumType) {
        this.checksumType = checksumType;
    }
}

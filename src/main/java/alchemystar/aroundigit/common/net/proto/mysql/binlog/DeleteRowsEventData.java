/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import alchemystar.aroundigit.common.net.handler.backend.codec.DecodeUtil;
import alchemystar.aroundigit.common.net.handler.backend.codec.DecoderConfig;
import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * DeleteRowsEventData
 *
 * @Author lizhuyang
 */
public class DeleteRowsEventData extends AbstractRowsEventData implements EventData {

    private long tableId;
    private BitSet includedColumns;

    private List<Object[]> rows;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        tableId = mm.readLong(6);
        mm.move(2); // reserved
        if (DecoderConfig.mayContainExtraInformation) {
            int extraInfoLength = mm.readUB2();
            mm.move(extraInfoLength - 2);
        }
        int numberOfColumns = (int) mm.readLength();
        includedColumns = DecodeUtil.readBitSet(mm, numberOfColumns, true);
        rows = decodeRows(mm);
    }

    private List<Object[]> decodeRows(MySQLMessage mm) {
        TableMapEventData tableMapEvent = DecoderConfig.getTableMap(getTableId());
        ColumnSet includedColumns = new ColumnSet(getIncludedColumns());
        List<Object[]> result = new LinkedList<Object[]>();
        // checksum the last 4bytes,if crc32
        while (mm.available() > DecoderConfig.checksumType.getLength()) {
            result.add(decodeRows(tableMapEvent, includedColumns, mm));
        }
        return result;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public BitSet getIncludedColumns() {
        return includedColumns;
    }

    public void setIncludedColumns(BitSet includedColumns) {
        this.includedColumns = includedColumns;
    }

    public List<Object[]> getRows() {
        return rows;
    }

    public void setRows(List<Object[]> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DeleteRowsEventData");
        sb.append("{tableId=").append(tableId);
        sb.append(", includedColumns=").append(includedColumns);
        sb.append(", rows=[");
        for (Object[] row : rows) {
            sb.append("\n    ").append(Arrays.toString(row)).append(",");
        }
        if (!rows.isEmpty()) {
            sb.replace(sb.length() - 1, sb.length(), "\n");
        }
        sb.append("]}");
        return sb.toString();
    }
}

/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alchemystar.aroundigit.common.net.handler.backend.codec.DecodeUtil;
import alchemystar.aroundigit.common.net.handler.backend.codec.DecoderConfig;
import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * UpdateRowsEventData
 *
 * @Author lizhuyang
 */
public class UpdateRowsEventData extends AbstractRowsEventData implements EventData {

    private long tableId;
    private BitSet includedColumnsBeforeUpdate;
    private BitSet includedColumns;

    private List<Map.Entry<Object[], Object[]>> rows;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        tableId = mm.readLong(6);
        mm.move(2); // skip 2 bytes,reserved
        if (DecoderConfig.mayContainExtraInformation) {
            int extraInfoLength = mm.readInteger(2);
            mm.move(extraInfoLength - 2);
        }
        int numberOfColums = (int) mm.readLength();
        includedColumnsBeforeUpdate = DecodeUtil.readBitSet(mm, numberOfColums, true);
        includedColumns = DecodeUtil.readBitSet(mm,numberOfColums,true);
        rows = decodeRows(mm);
    }

    private List<Map.Entry<Object[], Object[]>> decodeRows(MySQLMessage mm) {
        TableMapEventData tableMapEvent = DecoderConfig.getTableMap(tableId);
        ColumnSet includedColumnsBeforeUpdate = new ColumnSet(getIncludedColumnsBeforeUpdate());
        ColumnSet includedColumns = new ColumnSet(getIncludedColumns());
        List<Map.Entry<Object[], Object[]>> rows = new LinkedList<Map.Entry<Object[], Object[]>>();
        while (mm.available() > DecoderConfig.checksumType.getLength()) {
            rows.add(new AbstractMap.SimpleEntry<Object[], Object[]>(
                    decodeRows(tableMapEvent, includedColumnsBeforeUpdate, mm),
                    decodeRows(tableMapEvent, includedColumns, mm)
            ));
        }
        return rows;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public BitSet getIncludedColumnsBeforeUpdate() {
        return includedColumnsBeforeUpdate;
    }

    public void setIncludedColumnsBeforeUpdate(BitSet includedColumnsBeforeUpdate) {
        this.includedColumnsBeforeUpdate = includedColumnsBeforeUpdate;
    }

    public BitSet getIncludedColumns() {
        return includedColumns;
    }

    public void setIncludedColumns(BitSet includedColumns) {
        this.includedColumns = includedColumns;
    }

    public List<Map.Entry<Object[], Object[]>> getRows() {
        return rows;
    }

    public void setRows(List<Map.Entry<Object[], Object[]>> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UpdateRowsEventData");
        sb.append("{tableId=").append(tableId);
        sb.append(", includedColumnsBeforeUpdate=").append(includedColumnsBeforeUpdate);
        sb.append(", includedColumns=").append(includedColumns);
        sb.append(", rows=[");
        for (Map.Entry<Object[], Object[]> row : rows) {
            sb.append("\n    ").
                    append("{before=").append(Arrays.toString(row.getKey())).
                    append(", after=").append(Arrays.toString(row.getValue())).
                    append("},");
        }
        if (!rows.isEmpty()) {
            sb.replace(sb.length() - 1, sb.length(), "\n");
        }
        sb.append("]}");
        return sb.toString();
    }
}

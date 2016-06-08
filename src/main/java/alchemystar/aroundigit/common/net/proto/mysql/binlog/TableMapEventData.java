/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import java.util.BitSet;

import alchemystar.aroundigit.common.net.handler.backend.codec.DecodeUtil;
import alchemystar.aroundigit.common.net.handler.backend.codec.DecoderConfig;
import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * TableMapEventData
 *
 * The TABLE_MAP_EVENT defines the structure if the tables that are about to be changed.
 *
 * @Author lizhuyang
 */
public class TableMapEventData implements EventData {

    private long tableId;
    private String database;
    private String table;
    // column type
    private byte[] columnTypes;
    // column length
    private int[] columnMetadata;
    // 可为NULL的row bit位图
    private BitSet columnNullability;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        tableId = mm.readLong(6);
        mm.move(3); // 2 bytes reserved for future use + 1 for the length of database name
        database = mm.readStringWithNull();
        mm.move(1); //table name
        table = mm.readStringWithNull();
        int numberOfColumns = (int) mm.readLength();
        columnTypes = mm.readBytes(numberOfColumns);
        mm.readLength(); // metadata length
        columnMetadata = readMetadata(mm, columnTypes);
        columnNullability = DecodeUtil.readBitSet(mm, numberOfColumns, true);
        // 自身注册进tableMap
        DecoderConfig.putTableMap(tableId,this);
    }

    private int[] readMetadata(MySQLMessage mm, byte[] columnTypes) {
        int[] metadata = new int[columnTypes.length];
        for (int i = 0; i < columnTypes.length; i++) {
            switch (ColumnType.byCode(columnTypes[i] & 0xFF)) {
                case FLOAT:
                case DOUBLE:
                case BLOB:
                case GEOMETRY:
                    metadata[i] = mm.read();
                    break;
                case BIT:
                case VARCHAR:
                case NEWDECIMAL:
                    metadata[i] = mm.readUB2();
                    break;
                case SET:
                case ENUM:
                case STRING:
                    metadata[i] = DecodeUtil.bigEndianInteger(mm.readBytes(2), 0, 2);
                    break;
                case TIME_V2:
                case DATETIME_V2:
                case TIMESTAMP_V2:
                    metadata[i] = mm.read();
                    break;
                default:
                    metadata[i] = 0;
            }
        }
        return metadata;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public byte[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(byte[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public int[] getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(int[] columnMetadata) {
        this.columnMetadata = columnMetadata;
    }

    public BitSet getColumnNullability() {
        return columnNullability;
    }

    public void setColumnNullability(BitSet columnNullability) {
        this.columnNullability = columnNullability;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TableMapEventData");
        sb.append("{tableId=").append(tableId);
        sb.append(", database='").append(database).append('\'');
        sb.append(", table='").append(table).append('\'');
        sb.append(", columnTypes=").append(columnTypes == null ? "null" : "");
        for (int i = 0; columnTypes != null && i < columnTypes.length; ++i) {
            sb.append(i == 0 ? "" : ", ").append(columnTypes[i]);
        }
        sb.append(", columnMetadata=").append(columnMetadata == null ? "null" : "");
        for (int i = 0; columnMetadata != null && i < columnMetadata.length; ++i) {
            sb.append(i == 0 ? "" : ", ").append(columnMetadata[i]);
        }
        sb.append(", columnNullability=").append(columnNullability);
        sb.append('}');
        return sb.toString();
    }
}

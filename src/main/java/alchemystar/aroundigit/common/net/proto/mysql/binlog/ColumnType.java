/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import java.util.HashMap;
import java.util.Map;

/**
 * ColumnType
 *
 * @author lizhuyang
 */
public enum ColumnType {

    DECIMAL(0),
    TINY(1),
    SHORT(2),
    LONG(3),
    FLOAT(4),
    DOUBLE(5),
    NULL(6),
    TIMESTAMP(7),
    LONGLONG(8),
    INT24(9),
    DATE(10),
    TIME(11),
    DATETIME(12),
    YEAR(13),
    NEWDATE(14),
    VARCHAR(15),
    BIT(16),
    // (TIMESTAMP|DATETIME|TIME)_V2 data types appeared in MySQL 5.6.4
    // @see http://dev.mysql.com/doc/internals/en/date-and-time-data-type-representation.html
    TIMESTAMP_V2(17),
    DATETIME_V2(18),
    TIME_V2(19),
    NEWDECIMAL(246),
    ENUM(247),
    SET(248),
    TINY_BLOB(249),
    MEDIUM_BLOB(250),
    LONG_BLOB(251),
    BLOB(252),
    VAR_STRING(253),
    STRING(254),
    GEOMETRY(255);

    private int code;

    private ColumnType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static final Map<Integer, ColumnType> INDEX_BY_CODE;

    static {
        INDEX_BY_CODE = new HashMap<Integer, ColumnType>();
        for (ColumnType columnType : values()) {
            INDEX_BY_CODE.put(columnType.code, columnType);
        }
    }

    public static ColumnType byCode(int code) {
        return INDEX_BY_CODE.get(code);
    }

}

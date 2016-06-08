/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.handler.backend.resulthandler.ChecksumType;
import alchemystar.aroundigit.common.net.proto.mysql.binlog.EventType;
import alchemystar.aroundigit.common.net.proto.mysql.binlog.RowsQueryEventDecoder;
import alchemystar.aroundigit.common.net.proto.mysql.binlog.TableMapEventData;

/**
 * DecoderConfig
 *
 * @Author lizhuyang
 */
public class DecoderConfig {

    private static final Logger logger = LoggerFactory.getLogger(DecoderConfig.class);

    public static Map<EventType, EventDataDecoder> decoderMap;

    public static Map<Long, TableMapEventData> tableMapEventByTableId;

    public static ChecksumType checksumType = ChecksumType.NONE;

    public static boolean mayContainExtraInformation = true;

    // 初始化decoder
    static {
        decoderMap = new HashMap<EventType, EventDataDecoder>();
        tableMapEventByTableId = new HashMap<Long, TableMapEventData>();
        decoderMap.put(EventType.FORMAT_DESCRIPTION, new FormatDescriptionEventDecoder());
        decoderMap.put(EventType.ROTATE, new RotateEventDecoder());
        decoderMap.put(EventType.INTVAR, new IntVarEventDecoder());
        decoderMap.put(EventType.QUERY, new QueryEventDecoder());
        decoderMap.put(EventType.TABLE_MAP, new TableMapEventDecoder());
        decoderMap.put(EventType.XID, new XidEventDecoder());
        decoderMap.put(EventType.WRITE_ROWS, new WriteRowsEventDecoder());
        decoderMap.put(EventType.EXT_WRITE_ROWS, new WriteRowsEventDecoder());
        decoderMap.put(EventType.UPDATE_ROWS, new UpdateRowsEventDecoder());
        decoderMap.put(EventType.EXT_UPDATE_ROWS, new UpdateRowsEventDecoder());
        decoderMap.put(EventType.DELETE_ROWS, new DeleteRowsEventDecoder());
        decoderMap.put(EventType.EXT_DELETE_ROWS, new DeleteRowsEventDecoder());
        decoderMap.put(EventType.ROWS_QUERY, new RowsQueryEventDecoder());
        decoderMap.put(EventType.ANONYMOUS_GTID, new GtidEventDecoder());
        decoderMap.put(EventType.GTID, new GtidEventDecoder());
    }

    public static EventDataDecoder getDecoder(EventType eventType) {
        EventDataDecoder decoder = decoderMap.get(eventType);
        return decoder;
    }

    public static void putTableMap(Long tableId, TableMapEventData tableMapEventData) {
        tableMapEventByTableId.put(tableId, tableMapEventData);
    }

    public static TableMapEventData getTableMap(Long tableId) {
        TableMapEventData tableMapEventData = tableMapEventByTableId.get(tableId);
        if (tableMapEventData == null) {
            logger.error("no match tableMap,tableId=" + tableId);
            throw new RuntimeException("no match tableMap,tableId=" + tableId);
        }
        return tableMapEventData;
    }

}

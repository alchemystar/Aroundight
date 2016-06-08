/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.TableMapEventData;

/**
 * TableMapEventData
 *
 * @Author lizhuyang
 */
public class TableMapEventDecoder implements EventDataDecoder<TableMapEventData> {

    private static final Logger logger = LoggerFactory.getLogger(RotateEventDecoder.class);

    public TableMapEventData decode(byte[] data) {
        TableMapEventData eventData = new TableMapEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}


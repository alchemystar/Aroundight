/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.QueryEventData;

/**
 * QueryEventDecoder
 *
 * @Author lizhuyang
 */
public class QueryEventDecoder implements EventDataDecoder<QueryEventData> {

    private static final Logger logger = LoggerFactory.getLogger(QueryEventDecoder.class);

    public QueryEventData decode(byte[] data) {
        QueryEventData eventData = new QueryEventData();
        eventData.read(data);
        logger.debug(eventData.toString());
        return eventData;
    }
}

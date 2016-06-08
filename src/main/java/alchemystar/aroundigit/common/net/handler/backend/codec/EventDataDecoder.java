/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.backend.codec;

import alchemystar.aroundigit.common.net.proto.mysql.binlog.EventData;

/**
 * EventDataDecoder
 *
 * @Author lizhuyang
 */
public interface EventDataDecoder<T extends EventData> {

    T decode(byte[] data);
}

/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.handler.factory;

import alchemystar.aroundigit.common.net.handler.backend.BackendConnection;

/**
 * 连接工厂
 *
 * @Author lizhuyang
 */
public class BackendConnectionFactory {

    public BackendConnection getConnection() {
        BackendConnection connection = new BackendConnection();
        return connection;
    }
}

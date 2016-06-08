/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.factory;

import alchemystar.lancelot.common.net.handler.backend.BackendConnection;
import alchemystar.lancelot.common.net.handler.backend.pool.MySqlDataPool;

/**
 * 连接工厂
 *
 * @Author lizhuyang
 */
public class BackendConnectionFactory {

    private MySqlDataPool mySqlDataPool;

    public BackendConnectionFactory(MySqlDataPool mySqlDataPool) {
        this.mySqlDataPool = mySqlDataPool;
    }

    public BackendConnection getConnection() {
        BackendConnection connection = new BackendConnection(mySqlDataPool);
        return connection;
    }
}

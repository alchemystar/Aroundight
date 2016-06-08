/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.session;

import alchemystar.lancelot.common.net.handler.frontend.FrontendConnection;
import alchemystar.lancelot.common.net.route.RouteResultset;

/**
 * @Author lizhuyang
 */
public interface Session {
    /**
     * 取得源端连接
     */
    FrontendConnection getSource();

    /**
     * 取得当前目标端数量
     */
    int getTargetCount();

    /**
     * 开启一个会话执行
     */
    void execute(String sql, int type);

    /**
     * 提交一个会话执行
     */
    void commit();

    /**
     * 回滚一个会话执行
     */
    void rollback();

    /**
     * 取消一个正在执行中的会话
     *
     * @param sponsor
     */
    void cancel(FrontendConnection sponsor);

    /**
     * 终止会话，必须在关闭源端连接后执行该方法。
     */
    void terminate();
    /**
     * 关闭会话，必须在关闭源端连接后执行该方法。
     */
    void close();
}

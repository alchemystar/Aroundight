/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend;

/**
 * 后端连接状态枚举
 * @Author lizhuyang
 */
public interface BackendConnState {

    // 后端连接尚未初始化
    int BACKEND_NOT_AUTHED=0;
    // 后端连接初始化成功
    int BACKEND_AUTHED=1;

    // must 连续
    int RESULT_SET_FIELD_COUNT = 2;
    int RESULT_SET_FIELDS = 3;
    int RESULT_SET_EOF = 4;
    int RESULT_SET_ROW = 5;
    int RESULT_SET_LAST_EOF = 6;
}

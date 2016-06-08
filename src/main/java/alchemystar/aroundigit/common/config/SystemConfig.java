/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.config;

import alchemystar.aroundigit.common.net.proto.util.Isolations;

/**
 * SystemConfig
 * todo 捞取配置
 *
 * @Author lizhuyang
 */
public class SystemConfig {

    public static final int BackendInitialSize = 10;
    public static final int BackendMaxSize = 20;
    public static final int BackendInitialWaitTime = 60;
    public static final String MySqlHost = "127.0.0.1";
    public static final int ServerPort = 8090;
    public static final int MySqlPort = 3306;
    public static final String UserName = "root";
    public static final String PassWord = "123123123";

    public static final long ServerId = 2; //1
    // public static final int MySqlPort = 8080;
    // public static final String UserName = "pay";
    // public static final String PassWord = "MiraCle";

    public static final String Database = "";
    public static final int IdleCheckInterval = 5000;
    public static final int BackendConnectRetryTimes = 3;

    public static String DEFAULT_CHARSET = "utf8";
    public static int DEFAULT_TX_ISOLATION = Isolations.REPEATED_READ;

}

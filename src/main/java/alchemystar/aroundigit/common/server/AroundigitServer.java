/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.aroundigit.common.config.SystemConfig;
import alchemystar.aroundigit.common.net.handler.factory.BackendConnectionFactory;
import alchemystar.aroundigit.common.net.handler.factory.BackendHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 无毁的湖光 启动器
 *
 * @Author lizhuyang
 */
public class AroundigitServer extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(AroundigitServer.class);

    public static void main(String[] args) {
        AroundigitServer server = new AroundigitServer();
        try {
            server.start();
            while (true) {
                Thread.sleep(1000 * 300);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logger.info("Start The Aroundigit");
        startServer();
    }

    public void startServer() {
        BackendConnectionFactory factory = new BackendConnectionFactory();
        // acceptor
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new BackendHandlerFactory(factory));
        try {
            b.connect(SystemConfig.MySqlHost, SystemConfig.MySqlPort).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

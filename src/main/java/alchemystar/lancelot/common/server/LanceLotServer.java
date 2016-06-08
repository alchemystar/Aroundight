/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.config.SocketConfig;
import alchemystar.lancelot.common.config.SystemConfig;
import alchemystar.lancelot.common.net.handler.backend.pool.MySqlDataPool;
import alchemystar.lancelot.common.net.handler.backend.pool.MySqlDataSource;
import alchemystar.lancelot.common.net.handler.factory.FrontHandlerFactory;
import alchemystar.lancelot.loader.XmlLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 兰斯洛特 启动器
 *
 * @Author lizhuyang
 */
public class LanceLotServer extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(LanceLotServer.class);

    public static void main(String[] args) {
        LanceLotServer lancelot = new LanceLotServer();
        try {
            lancelot.start();
            while (true) {
                Thread.sleep(1000 * 300);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logger.info("Start The MySqlProxy");
        startProxy();
    }

    public void startProxy() {
        // acceptor
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // worker
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //加载配置信息
          //  XmlLoader.load();
            MySqlDataPool dataPool = new MySqlDataPool(SystemConfig.BackendInitialSize, SystemConfig.BackendMaxSize);
            dataPool.init();
            // dataPool wrapper
            MySqlDataSource dataSource = new MySqlDataSource(dataPool);
            ServerBootstrap b = new ServerBootstrap();
            // 这边的childHandler是用来管理accept的
            // 由于线程间传递的是byte[],所以内存池okay
            // 只需要保证分配ByteBuf和write在同一个线程(函数)就行了
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new FrontHandlerFactory(dataSource)).option(ChannelOption.ALLOCATOR,
                    PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SocketConfig.CONNECT_TIMEOUT_MILLIS)
                    .option(ChannelOption.SO_TIMEOUT, SocketConfig.SO_TIMEOUT);
            ChannelFuture f = b.bind(SystemConfig.ServerPort).sync();
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.error("监听失败" + e);
        }
    }
}

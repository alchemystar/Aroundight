/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.config.SocketConfig;
import alchemystar.lancelot.common.config.SystemConfig;
import alchemystar.lancelot.common.net.exception.RetryConnectFailException;
import alchemystar.lancelot.common.net.handler.backend.BackendHeadHandler;
import alchemystar.lancelot.common.net.handler.backend.BackendConnection;
import alchemystar.lancelot.common.net.handler.factory.BackendConnectionFactory;
import alchemystar.lancelot.common.net.handler.factory.BackendHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * MySql 连接池
 *
 * @Author lizhuyang
 */
public class MySqlDataPool {

    private static final Logger logger = LoggerFactory.getLogger(MySqlDataPool.class);

    // 当前连接池中空闲的连接数
    private int idleCount;
    // 最大连接数
    private final int maxPoolSize;
    // 初始化连接数
    private int initSize;
    // 连接池
    private final BackendConnection[] items;
    // Backend Loop Group
    private EventLoopGroup backendGroup;
    // Backend Bootstrap
    private Bootstrap b;
    // Backend Connection Factory
    private BackendConnectionFactory factory;
    // 线程间同步的闩锁
    private CountDownLatch latch;
    // get/put的锁
    private final ReentrantLock lock;
    // 当前连接池是否被初始化成功的标识
    private final AtomicBoolean initialized;
    // data pool的command allocator
    private ByteBufAllocator allocator;

    public MySqlDataPool(int initSize, int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        this.initSize = initSize;
        this.idleCount = 0;
        items = new BackendConnection[maxPoolSize];
        backendGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        latch = new CountDownLatch(initSize);
        lock = new ReentrantLock();
        initialized = new AtomicBoolean(false);
        allocator = new UnpooledByteBufAllocator(false);
    }

    public void init() {
        factory = new BackendConnectionFactory(this);
        // 采用PooledBuf来减少GC
        b.group(backendGroup).channel(NioSocketChannel.class).handler(new BackendHandlerFactory(factory))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        setOption(b);
        initBackends();
        initIdleCheck();
        markInit();
    }

    /**
     * 初始化后端连接
     */
    private void initBackends() {
        List<ChannelFuture> futureList = new ArrayList<ChannelFuture>();
        for (int i = 0; i < initSize; i++) {
            ChannelFuture future = b.connect(SystemConfig.MySqlHost, SystemConfig.MySqlPort);
            futureList.add(future);
        }
        try {
            // awit with time out
            latch.await(SystemConfig.BackendInitialWaitTime, TimeUnit.SECONDS);
            // if reach here,the backend has been initialized
            for(ChannelFuture future : futureList){
                future.sync();
                recycle(getInitBackendFromFuture(future));
            }
            // for gc
            latch = null;
            logger.info("the data pool start up");
        } catch (Exception e) {
            logger.error("latch fail", e);
        }
    }

    private void markInit() {
        if (initialized.compareAndSet(false, true)) {
            initialized.set(true);
        }
    }

    public BackendConnection getBackend() {
        BackendConnection backend = null;
       lock.lock();
        try {
            // idleCount 初始为0
            if (idleCount >= 1 && items[idleCount-1] != null) {
                backend = items[idleCount-1];
                idleCount--;
                return backend;
            }
        } finally {
            lock.unlock();
        }
        // must create new connection
        logger.info("create new conneciton");
        backend = createNewConnection();
        return backend;
    }

    private BackendConnection createNewConnection() {
        for (int i = 0; i < SystemConfig.BackendConnectRetryTimes; i++) {
            ChannelFuture future = b.connect(SystemConfig.MySqlHost, SystemConfig.MySqlPort);
            BackendConnection backend = getBackendFromFuture(future);
            if (backend != null) {
                return backend;
            }
        }
        throw new RetryConnectFailException("Retry Connect Error Host:" + SystemConfig.MySqlHost + " "
                + "Port:" + SystemConfig.MySqlPort);
    }

    private BackendConnection getInitBackendFromFuture(ChannelFuture future){
        // beacuse the init latch in the outside,we don't need latch await here
        try {
            future.sync();
            BackendHeadHandler firstHandler =
                    (BackendHeadHandler) future.channel().pipeline().get(BackendHeadHandler.HANDLER_NAME);
            return firstHandler.getSource();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BackendConnection getBackendFromFuture(ChannelFuture future) {
        try {
            // must wait to init the channel
            future.sync();
            BackendHeadHandler firstHandler =
                    (BackendHeadHandler) future.channel().pipeline().get(BackendHeadHandler.HANDLER_NAME);
            firstHandler.getSource().syncLatch.await();
            return firstHandler.getSource();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void recycle(BackendConnection backend) {
        putBackend(backend);
    }

    public void discard(BackendConnection backend){
        logger.info("backendConnection discard it");
    }


    public void putBackend(BackendConnection backend) {
        lock.lock();
        try {
            if (backend.isAlive()) {
                if (idleCount < maxPoolSize) {
                    items[idleCount] = backend;
                    idleCount++;
                } else {
                    backend.close();
                    logger.info("backendConnection too much,so close it");
                }
            } else {
                logger.info("backendConnection not alive,so discard it");
            }

        } finally {
            lock.unlock();
        }
    }


    public void countDown() {
        latch.countDown();
    }

    /**
     * 初始化IdleCheck
     */
    private void initIdleCheck() {
        //  backendGroup.scheduleAtFixedRate(new IdleCheckTask(), SystemConfig.IdleCheckInterval, SystemConfig
        // .IdleCheckInterval,TimeUnit.MILLISECONDS);
    }

    private void setOption(Bootstrap bootstrap) {

        bootstrap.option(ChannelOption.SO_RCVBUF, SocketConfig.Backend_Socket_Recv_Buf);
        bootstrap.option(ChannelOption.SO_SNDBUF, SocketConfig.Backend_Socket_Send_Buf);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,SocketConfig.CONNECT_TIMEOUT_MILLIS);
        bootstrap.option(ChannelOption.SO_TIMEOUT,SocketConfig.SO_TIMEOUT);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK,
                1024 * 1024);
    }

    /**
     * IdleCheck
     */
    private class IdleCheckTask implements Runnable {
        public void run() {
            idleCheck();
        }

        private void idleCheck() {
            // 伪代码 类Druid的最小锁时间实现
            // 在这个地方拿出连接
            // 心跳command用当前allocate(Unpooled)来分配,防内存泄露
            // postCommand(HeartBeat)
            // fireCmd
            // markInHeartBeat
            // 在commonHandler中
            // if(marInHeartBeat) then read okay
            // then recycle
            // heartBeat完成,锁只在一处
            logger.info("we now do idle check");
        }
    }

    public boolean isInited() {
        return initialized.get();
    }
}

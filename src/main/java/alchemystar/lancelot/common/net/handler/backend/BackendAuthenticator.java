/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.backend;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.config.SystemConfig;
import alchemystar.lancelot.common.net.exception.ErrorPacketException;
import alchemystar.lancelot.common.net.exception.UnknownCharsetException;
import alchemystar.lancelot.common.net.exception.UnknownPacketException;
import alchemystar.lancelot.common.net.handler.frontend.FrontendAuthenticator;
import alchemystar.lancelot.common.net.proto.mysql.AuthPacket;
import alchemystar.lancelot.common.net.proto.mysql.BinaryPacket;
import alchemystar.lancelot.common.net.proto.mysql.ErrorPacket;
import alchemystar.lancelot.common.net.proto.mysql.HandshakePacket;
import alchemystar.lancelot.common.net.proto.mysql.OkPacket;
import alchemystar.lancelot.common.net.proto.util.Capabilities;
import alchemystar.lancelot.common.net.proto.util.CharsetUtil;
import alchemystar.lancelot.common.net.proto.util.SecurityUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 后端连接处理器
 *
 * @Author lizhuyang
 */
public class BackendAuthenticator extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BackendAuthenticator.class);

    private static final long CLIENT_FLAGS = getClientFlags();
    private static final long MAX_PACKET_SIZE = 1024 * 1024 * 16;

    private int state = BackendConnState.BACKEND_NOT_AUTHED;

    private BackendConnection source;

    public BackendAuthenticator(BackendConnection source) {
        this.source = source;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (state) {
            case (BackendConnState.BACKEND_NOT_AUTHED):
                // init source's ctx here
                source.setCtx(ctx);
                // 处理握手包并发送auth包
                auth(ctx, msg);
                // 推进连接状态
                state = BackendConnState.BACKEND_AUTHED;
                break;
            case (BackendConnState.BACKEND_AUTHED):
                authOk(ctx, msg);
                break;
            default:
                break;
        }
    }

    private void authOk(ChannelHandlerContext ctx, Object msg) {
        BinaryPacket bin = (BinaryPacket) msg;
        switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT:
                afterSuccess();
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                throw new ErrorPacketException("Auth not Okay");
            default:
                throw new UnknownPacketException(bin.toString());
        }
        // to wake up the start up thread
        source.countDown();
        // replace the commandHandler of Authenticator
        ctx.pipeline().replace(this, "BackendCommandHandler", new BackendCommandHandler(source));

    }

    /**
     * 连接和验证成功以后
     */
    private void afterSuccess() {
        // todo
        //  if (dsc.getSqlMode() != null) {
        //      sendSqlMode();
        //  }
        // 为防止握手阶段字符集编码交互无效，连接成功之后做一次字符集编码同步。
        // sendCharset(charsetIndex);
        logger.info("auth okay");
    }

    private void auth(ChannelHandlerContext ctx, Object msg) {
        HandshakePacket hsp = new HandshakePacket();
        hsp.read((BinaryPacket) msg);
        source.setId(hsp.threadId);
        int ci = hsp.serverCharsetIndex & 0xff;
        if ((source.charset = CharsetUtil.getCharset(ci)) != null) {
            source.charsetIndex = ci;
        } else {
            throw new UnknownCharsetException("charset:" + ci);
        }
        try {
            auth(hsp, ctx);
        } catch (Exception e) {
            logger.error("auth packet errorMessage", e);
        }
    }

    private void auth(HandshakePacket hsp, ChannelHandlerContext ctx)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        AuthPacket ap = new AuthPacket();
        ap.packetId = 1;
        ap.clientFlags = CLIENT_FLAGS;
        ap.maxPacketSize = MAX_PACKET_SIZE;
        ap.charsetIndex = source.charsetIndex;
        // todo config
        ap.user = SystemConfig.UserName;
        String passwd = SystemConfig.PassWord;
        if (passwd != null && passwd.length() > 0) {
            byte[] password = passwd.getBytes(source.charset);
            byte[] seed = hsp.seed;
            byte[] restOfScramble = hsp.restOfScrambleBuff;
            byte[] authSeed = new byte[seed.length + restOfScramble.length];
            System.arraycopy(seed, 0, authSeed, 0, seed.length);
            System.arraycopy(restOfScramble, 0, authSeed, seed.length, restOfScramble.length);
            ap.password = SecurityUtil.scramble411(password, authSeed);
        }
        // todo config
        ap.database = SystemConfig.Database;
        ap.write(ctx);

    }

    /**
     * 与MySQL连接时的一些特性指定
     */
    private static long getClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // 不允许MULTI协议
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }
}





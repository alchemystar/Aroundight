/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.session;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.net.handler.backend.BackendConnection;
import alchemystar.lancelot.common.net.handler.frontend.FrontendConnection;
import alchemystar.lancelot.common.net.handler.node.CommitExecutor;
import alchemystar.lancelot.common.net.handler.node.MultiNodeExecutor;
import alchemystar.lancelot.common.net.handler.node.ResponseHandler;
import alchemystar.lancelot.common.net.handler.node.RollBackExecutor;
import alchemystar.lancelot.common.net.handler.node.SingleNodeExecutor;
import alchemystar.lancelot.common.net.proto.util.ErrorCode;
import alchemystar.lancelot.common.net.route.RouteResultset;
import alchemystar.lancelot.common.net.route.RouteResultsetNode;
import alchemystar.lancelot.route.LancelotStmtParser;
import io.netty.channel.ChannelHandlerContext;

/**
 * 前端会话过程
 *
 * @Author lizhuyang
 */
public class FrontendSession implements Session {

    private static final Logger logger = LoggerFactory.getLogger(FrontendSession.class);

    private FrontendConnection source;
    // 事务是否被打断
    private volatile boolean txInterrupted;
    // 被打断保存的信息
    private volatile String txInterrputMsg = "";

    private final ConcurrentHashMap<RouteResultsetNode, BackendConnection> target;

    private final SingleNodeExecutor singleNodeExecutor;
    private final MultiNodeExecutor multiNodeExecutor;
    private final CommitExecutor commitExecutor;
    private final RollBackExecutor rollBackExecutor;
    // 处理当前sql的handler,可以是single也有可能是multi
    private ResponseHandler responseHandler;

    public FrontendSession(FrontendConnection source) {
        this.source = source;
        target = new ConcurrentHashMap<RouteResultsetNode, BackendConnection>();
        singleNodeExecutor = new SingleNodeExecutor(this);
        multiNodeExecutor = new MultiNodeExecutor(this);
        commitExecutor = new CommitExecutor(this);
        rollBackExecutor = new RollBackExecutor(this);
    }

    public FrontendConnection getSource() {
        return source;
    }

    public int getTargetCount() {
        return target.size();
    }

    public void execute(String sql, int type) {
        // 状态检查
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction errorMessage, need to rollback." + txInterrputMsg);
            return;
        }
        RouteResultset rrs = route(sql, type);
        if (rrs.getNodeCount() == 0) {
            writeErrMessage(ErrorCode.ER_PARSE_ERROR, "parse sql and 0 node get");
            return;
        } else if (rrs.getNodeCount() == 1) {
            responseHandler = singleNodeExecutor;
            singleNodeExecutor.execute(rrs);
        } else {
            responseHandler = multiNodeExecutor;
            multiNodeExecutor.execute(rrs);
        }
    }

    private RouteResultset route(String sql, int type) {
        RouteResultset routeResultset = LancelotStmtParser.parser(sql,type);
        if(routeResultset == null){
            routeResultset = new RouteResultset();
            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            RouteResultsetNode node = new RouteResultsetNode("1",sql,type);
            nodes[0] = node;
            routeResultset.setNodes(nodes);
        }
        return routeResultset;
    }

    public void commit() {
        // 异步回调处理器
        responseHandler = commitExecutor;
        commitExecutor.commit();
    }

    public void rollback() {
        // 状态检查
        if (txInterrupted) {
            txInterrupted = false;
        }
        // 异步回调处理器
        responseHandler = rollBackExecutor;
        rollBackExecutor.rollback();
        return;
    }

    public void cancel(FrontendConnection sponsor) {

    }

    public void terminate() {

    }

    public void close() {
        for (BackendConnection backend : target.values()) {
            backend.recycle();
        }
        target.clear();
        logger.info("session closed");
    }

    public void writeErrMessage(int errno, String msg) {
        logger.warn(String.format("[FrontendConnection]ErrorNo=%d,ErrorMsg=%s", errno, msg));
        source.writeErrMessage((byte) 1, errno, msg);
    }

    public void writeOk() {
        source.writeOk();
    }

    public void setSource(FrontendConnection source) {
        this.source = source;
    }

    public BackendConnection getTarget(RouteResultsetNode key) {
        BackendConnection backend = target.get(key);
        if (backend == null) {
            backend = source.getStateSyncBackend();
            target.put(key, backend);
        }
        return backend;
    }

    public void release(){
        for(BackendConnection backend : target.values()){
            // recycle本身做了alive判断
            backend.recycle();
        }
        logger.debug("session has been released");
        target.clear();
    }

    /**
     * 设置是否需要中断当前事务
     */
    public void setTxInterrupt(String txInterrputMsg) {
        if (!source.isAutocommit() && !txInterrupted) {
            txInterrupted = true;
            this.txInterrputMsg = txInterrputMsg;
        }
    }

    public ConcurrentHashMap<RouteResultsetNode, BackendConnection> getTarget() {
        return target;
    }

    public ChannelHandlerContext getCtx() {
        return source.getCtx();
    }

    public SingleNodeExecutor getSingleNodeExecutor() {
        return singleNodeExecutor;
    }

    public MultiNodeExecutor getMultiNodeExecutor() {
        return multiNodeExecutor;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public String getTxInterrputMsg() {
        return txInterrputMsg;
    }

    public void setTxInterrputMsg(String txInterrputMsg) {
        this.txInterrputMsg = txInterrputMsg;
    }

    public boolean isTxInterrupted() {
        return txInterrupted;
    }

    public void setTxInterrupted(boolean txInterrupted) {
        this.txInterrupted = txInterrupted;
    }
}

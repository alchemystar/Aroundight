/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.handler.node;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.lancelot.common.net.handler.backend.BackendConnection;
import alchemystar.lancelot.common.net.handler.backend.cmd.Command;
import alchemystar.lancelot.common.net.handler.session.FrontendSession;
import alchemystar.lancelot.common.net.proto.mysql.BinaryPacket;
import alchemystar.lancelot.common.net.proto.mysql.ErrorPacket;
import alchemystar.lancelot.common.net.proto.mysql.OkPacket;
import alchemystar.lancelot.common.net.route.RouteResultset;
import alchemystar.lancelot.common.net.route.RouteResultsetNode;

/**
 * MultiNodeExecutor
 *
 * @Author lizhuyang
 */
public class MultiNodeExecutor extends MultiNodeHandler {

    private static final Logger logger = LoggerFactory.getLogger(MultiNodeExecutor.class);

    // 对应的前端连接
    private FrontendSession session;
    // 影响的行数,Okay包用
    private volatile long affectedRows;
    // 是否fieldEof被返回了
    private volatile boolean fieldEofReturned;

    public MultiNodeExecutor(FrontendSession session) {
        fieldEofReturned = false;
        this.session = session;
    }

    /**
     * 异步执行
     * todo 如果要做连接数限流的话,backend.recycle的时候在lastEof获取的时候,执行
     * 的时候需要加锁,来限制并发
     */
    public void execute(RouteResultset rrs) {
        // 在这里init了count
        reset(rrs.getNodes().length);
        for (RouteResultsetNode node : rrs.getNodes()) {
            BackendConnection backend = session.getTarget(node);
            execute(backend, node);
        }
    }

    protected void reset(int initCount) {
        super.reset(initCount);
        affectedRows = 0L;
        fieldEofReturned = false;
    }

    public void execute(BackendConnection backend, RouteResultsetNode node) {
        // convertToCommand
        Command command = session.getSource().getFrontendCommand(node.getStatement(), node.getSqlType());
        // add to backend queue
        backend.postCommand(command);
        // fire it
        backend.fireCmd();
    }

    public void fieldListResponse(List<BinaryPacket> fieldList) {
        lock.lock();
        try {
            if(!isFailed.get()) {
                // 如果还没有传过fieldList的话,则传递
                if (!fieldEofReturned) {
                    writeFiledList(fieldList);
                    fieldEofReturned = true;
                }
            }
        } finally {
            lock.unlock();
        }

    }

    private void writeFiledList(List<BinaryPacket> fieldList) {
        for (BinaryPacket bin : fieldList) {
            bin.packetId = ++packetId;
            bin.write(session.getCtx());
        }
        fieldList.clear();
    }

    public void errorResponse(BinaryPacket bin) {
        ErrorPacket err = new ErrorPacket();
        err.read(bin);
        String errorMessage = new String(err.message);
        logger.error("errorMessage packet " + errorMessage);
        lock.lock();
        try {
            // 但凡有一个error,就记录error信息
            if (isFailed.compareAndSet(false, true)) {
                this.setFailed(errorMessage,err.errno);
            }
            // try connection and finish conditon check
            // todo close the relative conn
            // canClose(conn, true);
            // 最后一个才发送
            if(decrementCountBy()){
               notifyFailure();
            }
        } finally {
            lock.unlock();
        }

    }

    public void okResponse(BinaryPacket bin) {
        OkPacket ok = new OkPacket();
        ok.read(bin);
        lock.lock();
        try {
            affectedRows += ok.affectedRows;
            if (decrementCountBy()) {
                // OK Response 只有在最后一个Okay到达且前面都不出错的时候才传送
                if (!isFailed.get()) {
                    ok.affectedRows = affectedRows;
                    // lastInsertId
                    logger.info("last insert id =" + ok.insertId);
                    session.getSource().setLastInsertId(ok.insertId);
                    ok.write(session.getCtx());
                    if(session.getSource().isAutocommit()){
                        session.release();
                    }
                }else{
                    // if 出错
                    notifyFailure();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void rowResponse(BinaryPacket bin) {
        lock.lock();
        try {
            if (!isFailed.get() && fieldEofReturned) {
                logger.info("rows");
                bin.packetId = ++packetId;
                bin.write(session.getCtx());
            }
        } finally {
            lock.unlock();
        }
    }

    // last eof response
    public void lastEofResponse(BinaryPacket bin) {
        lock.lock();
        try {
            logger.info("last eof ");
            if (decrementCountBy()) {
                if (!isFailed.get()) {
                    bin.packetId = ++packetId;
                    logger.info("write eof okay");
                    bin.write(session.getCtx());
                    // 如果是自动提交,则释放session
                    if(session.getSource().isAutocommit()){
                        session.release();
                    }
                }else{
                    notifyFailure();
                }
            }
        } finally {
            lock.unlock();
        }

    }

    private void notifyFailure(){
        ErrorPacket error = new ErrorPacket();
        error.message = errorMessage.getBytes();
        error.errno = errno;
        error.packetId = ++packetId;// ERROR_PACKET
        error.write(session.getCtx());
        // 如果是自动提交,则释放session
        if(session.getSource().isAutocommit()){
            session.release();
        }
    }

    public FrontendSession getSession() {
        return session;
    }

    public void setSession(FrontendSession session) {
        this.session = session;
    }

    public long getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(long affectedRows) {
        this.affectedRows = affectedRows;
    }

    public boolean isFieldEofReturned() {
        return fieldEofReturned;
    }

    public void setFieldEofReturned(boolean fieldEofReturned) {
        this.fieldEofReturned = fieldEofReturned;
    }

}

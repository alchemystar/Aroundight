/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.exception;

/**
 * @author lizhuyang
 */
public class UnknownDataNodeException extends RuntimeException {
    private static final long serialVersionUID = -3752985849571697432L;

    public UnknownDataNodeException() {
        super();
    }

    public UnknownDataNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownDataNodeException(String message) {
        super(message);
    }

    public UnknownDataNodeException(Throwable cause) {
        super(cause);
    }

}

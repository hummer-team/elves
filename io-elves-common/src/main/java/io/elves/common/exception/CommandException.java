package io.elves.common.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class CommandException extends RuntimeException {

    private static final long serialVersionUID = -333133765766799845L;
    private final HttpResponseStatus status;
    private final String message;
    private final transient Throwable cause;
    private final Object returnObj;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CommandException(HttpResponseStatus status, String message, Throwable cause, Object returnObj) {
        super(message, cause);
        this.status = status;
        this.message = message;
        this.cause = cause;
        this.returnObj = returnObj;
    }

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CommandException(HttpResponseStatus status, String message, Object returnObj) {
        super(message);
        this.status = status;
        this.message = message;
        this.cause = null;
        this.returnObj = returnObj;
    }

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CommandException(HttpResponseStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
        this.cause = null;
        this.returnObj = null;
    }

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CommandException(HttpResponseStatus status) {
        this.status = status;
        this.message = null;
        this.cause = null;
        this.returnObj = null;
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @since 1.7
     */
    public CommandException(String message
            , Throwable cause
            , boolean enableSuppression
            , boolean writableStackTrace
            , HttpResponseStatus status, Object returnObj) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
        this.message = message;
        this.cause = cause;
        this.returnObj = returnObj;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public Object getReturnObj() {
        return returnObj;
    }
}

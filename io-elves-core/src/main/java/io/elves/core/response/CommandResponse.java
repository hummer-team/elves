package io.elves.core.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.MDC;

import static io.elves.core.ElvesConstants.REQUEST_ID_KEY;

/**
 * Command response representation of command center.
 *
 * @param <R> type of the result
 * @author lee
 */
public class CommandResponse<R> {

    private final int code;
    private final R data;
    private final transient Throwable exception;
    private String message;
    private String trackId;
    private final transient HttpResponseStatus status;

    private CommandResponse(R data) {
        this(data, 0, null, HttpResponseStatus.OK);
    }

    private CommandResponse(R data, HttpResponseStatus status) {
        this(data, 0, null, status);
    }

    private CommandResponse(R data, int code, Throwable exception, HttpResponseStatus status) {
        this.code = code;
        this.data = data;
        this.exception = exception;
        this.status = status;
    }

    /**
     * Construct a successful response with given object.
     *
     * @param result result object
     * @param <T>    type of the result
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ok(T result) {
        return new CommandResponse<T>(result);
    }

    public static <T> CommandResponse<T> ok(T result, HttpResponseStatus status) {
        return new CommandResponse<T>(result, status);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex cause of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> failure(Throwable ex) {
        return new CommandResponse<T>(null
                , -50000
                , ex
                , HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex     cause of the failure
     * @param result additional message of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> failure(Throwable ex, T result) {
        return new CommandResponse<T>(result, -50000, ex, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public int getCode() {
        return code;
    }

    public R getData() {
        return data;
    }

    public Throwable getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }

    public String getExceptionMessage() {
        return exception == null ? null : exception.getMessage();
    }

    public String getTrackId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}

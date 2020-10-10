package io.elves.core.response;

/**
 * Command response representation of command center.
 *
 * @param <R> type of the result
 * @author lee
 */
public class CommandResponse<R> {

    private final int code;
    private final R data;
    private final Throwable exception;
    private String message;

    private CommandResponse(R data) {
        this(data, 0, null);
    }

    private CommandResponse(R data, int code, Throwable exception) {
        this.code = code;
        this.data = data;
        this.exception = exception;
    }

    /**
     * Construct a successful response with given object.
     *
     * @param result result object
     * @param <T>    type of the result
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofSuccess(T result) {
        return new CommandResponse<T>(result);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex cause of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(Throwable ex) {
        return new CommandResponse<T>(null, -50000, ex);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex     cause of the failure
     * @param result additional message of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(Throwable ex, T result) {
        return new CommandResponse<T>(result, -50000, ex);
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
}

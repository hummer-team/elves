package io.elves.core.command;

import io.elves.core.context.RequestContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collection;


/**
 * global exception intercept
 *
 * @author edz
 */
public interface GlobalExceptionHandler {
    /**
     * include exception
     */
    Collection<Class<? extends Throwable>> filter();

    /**
     * custom handler logic
     *
     * @param e       Throwable
     * @param context request context
     */
    void handler(Throwable e, RequestContext context);

    /**
     * response error code
     */
    default HttpResponseStatus code() {
        return HttpResponseStatus.SERVICE_UNAVAILABLE;
    }
}

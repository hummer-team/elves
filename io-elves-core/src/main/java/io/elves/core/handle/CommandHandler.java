package io.elves.core.handle;

import io.elves.core.context.RequestContext;
import io.elves.core.response.CommandResponse;

public interface CommandHandler<R> {
    /**
     * init command,this method is executed before {@link #handle(RequestContext)}
     */
    default void init() {

    }

    /**
     * implement business process
     *
     * @param context request context
     * @return
     */
    CommandResponse<R> handle(RequestContext context);

    /**
     * destroy resource
     */
    default void destroy() {
    }
}

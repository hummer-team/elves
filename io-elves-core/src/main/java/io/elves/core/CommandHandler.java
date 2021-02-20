package io.elves.core;

import io.elves.core.context.RequestContextInner;
import io.elves.core.response.CommandResponse;

public interface CommandHandler<R> {
    /**
     * init command,this method is executed before {@link #handle(RequestContextInner)}
     */
    default void init() {

    }

    /**
     * implement business process
     *
     * @param context request context
     * @return
     */
    CommandResponse<R> handle(RequestContextInner context);

    /**
     * destroy resource
     */
    default void destroy() {
    }
}

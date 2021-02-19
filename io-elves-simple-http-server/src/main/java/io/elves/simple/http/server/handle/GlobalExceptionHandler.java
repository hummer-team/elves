package io.elves.simple.http.server.handle;

import com.google.common.collect.Lists;
import io.elves.core.command.GlobalException;
import io.elves.core.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@GlobalException
public class GlobalExceptionHandler implements io.elves.core.command.GlobalExceptionHandler {

    /**
     * include exception
     */
    @Override
    public Collection<Class<? extends Throwable>> filter() {
        return Lists.newArrayList(NullPointerException.class);
    }

    /**
     * custom handler logic
     *
     * @param e       Throwable
     * @param context request context
     */
    @Override
    public void handler(Throwable e, RequestContext context) {
        log.error("handler global exception,", e);
    }
}

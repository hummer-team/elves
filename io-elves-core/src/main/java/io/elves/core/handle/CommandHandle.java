package io.elves.core.handle;

import io.elves.core.context.RequestContext;
import io.elves.core.response.CommandResponse;

public interface CommandHandle<R> {
    CommandResponse<R> handle(RequestContext request);
}

package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandle;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;

@CommandMapping(name = "/hell", httpMethod = HttpMethod.GET)
public class HellwordCommandHandle implements CommandHandle<String> {
    @Override
    public CommandResponse<String> handle(RequestContext request) {
        return CommandResponse.ofSuccess("hellword");
    }
}

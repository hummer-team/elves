package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandle;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@CommandMapping(name = "/hell3", httpMethod = HttpMethod.POST)
public class HellwordCommandHandlerV3 implements CommandHandle<Map<String, String>> {

    @Override
    public CommandResponse<Map<String, String>> handle(RequestContext context) {
        return CommandResponse.ofSuccess(context.getParameters());
    }
}

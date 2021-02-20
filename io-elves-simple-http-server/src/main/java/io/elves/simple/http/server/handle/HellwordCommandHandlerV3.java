package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.CommandHandler;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@CommandActionMapping(name = "/hell3", httpMethod = HttpMethod.POST)
public class HellwordCommandHandlerV3 implements CommandHandler<Map<String, Object>> {

    @Override
    public CommandResponse<Map<String, Object>> handle(RequestContext context) {
        return CommandResponse.ok(context.getParameters());
    }
}

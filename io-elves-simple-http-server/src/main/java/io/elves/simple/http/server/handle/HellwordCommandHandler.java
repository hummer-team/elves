package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandler;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;

@CommandActionMapping(name = "/hell", httpMethod = HttpMethod.GET)
public class HellwordCommandHandler implements CommandHandler<String> {
    @Override
    public CommandResponse<String> handle(RequestContext context) {
        return CommandResponse.ok("hellword");
    }
}

package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.CommandHandler;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;

import static io.elves.core.ElvesConstants.JSON_CODER;

@CommandActionMapping(name = "/hell", httpMethod = HttpMethod.GET,respEncoderType = JSON_CODER)
public class HellwordCommandHandler implements CommandHandler<String> {
    @Override
    public CommandResponse<String> handle(RequestContext context) {
        return CommandResponse.ok("hellword");
    }
}

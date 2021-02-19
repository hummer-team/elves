package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandler;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;
import io.elves.simple.http.server.dto.HellwordReq;
import io.elves.simple.http.server.dto.HellwordResp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CommandActionMapping(name = "/hell", httpMethod = HttpMethod.POST)
public class HellwordCommandHandlerV2 implements CommandHandler<HellwordResp> {
    @Override
    public CommandResponse<HellwordResp> handle(RequestContext context) {
        HellwordResp resp = new HellwordResp();
        resp.setName("hi:" + ((HellwordReq) context.body(HellwordReq.class)).getName());
        return CommandResponse.ok(resp);
    }
}

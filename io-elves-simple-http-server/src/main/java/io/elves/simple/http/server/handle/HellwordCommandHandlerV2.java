package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandle;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;
import io.elves.simple.http.server.dto.HellwordReq;
import io.elves.simple.http.server.dto.HellwordResp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CommandMapping(name = "/hell", httpMethod = HttpMethod.POST)
public class HellwordCommandHandlerV2 implements CommandHandle<HellwordResp> {
    @Override
    public CommandResponse<HellwordResp> handle(RequestContext context) {
        HellwordResp resp = new HellwordResp();
        resp.setName("hi:" + ((HellwordReq) context.body(HellwordReq.class)).getName());
        return CommandResponse.ofSuccess(resp);
    }
}

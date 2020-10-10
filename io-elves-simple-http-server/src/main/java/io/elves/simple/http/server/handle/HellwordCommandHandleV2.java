package io.elves.simple.http.server.handle;

import com.alibaba.fastjson.JSON;
import io.elves.core.command.CommandMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandle;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;
import io.elves.simple.http.server.dto.HellwordReq;
import io.elves.simple.http.server.dto.HellwordResp;


@CommandMapping(name = "/hell", httpMethod = HttpMethod.POST)
public class HellwordCommandHandleV2 implements CommandHandle<HellwordResp> {
    @Override
    public CommandResponse<HellwordResp> handle(RequestContext request) {
        HellwordReq req = JSON.parseObject(request.getBody(), HellwordReq.class);
        HellwordResp resp = new HellwordResp();
        resp.setName("hi:" + req.getName());
        return CommandResponse.ofSuccess(resp);
    }
}

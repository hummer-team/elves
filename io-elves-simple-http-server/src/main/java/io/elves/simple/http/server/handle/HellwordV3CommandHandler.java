package io.elves.simple.http.server.handle;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.command.CommandHandlerMapping;
import io.elves.core.request.HttpMethod;
import io.elves.core.response.CommandResponse;
import io.elves.simple.http.server.dto.HellwordReq;
import io.elves.simple.http.server.dto.HellwordResp;

import static io.elves.core.ElvesConstants.JSON_CODER;

@CommandHandlerMapping(name = "/v1")
public class HellwordV3CommandHandler {

    @CommandActionMapping(name = "/hh", httpMethod = HttpMethod.GET)
    public String handler() {
        return "ssss";
    }

    @CommandActionMapping(name = "/hh2", httpMethod = HttpMethod.GET)
    public String handler2() {
        return "ssss2";
    }

    @CommandActionMapping(name = "/hh3", httpMethod = HttpMethod.GET)
    public String handler3() {
        return "ssss3";
    }

    @CommandActionMapping(name = "/hh4"
            , httpMethod = HttpMethod.GET
            , respEncoderType = JSON_CODER)
    public CommandResponse<HellwordResp> handler4() {
        HellwordResp resp = new HellwordResp();
        resp.setName("sssssssssss");
        return CommandResponse.ok(resp);
    }

    @CommandActionMapping(name = "/hh5"
            , httpMethod = HttpMethod.GET
            , respEncoderType = JSON_CODER)
    public CommandResponse<HellwordResp> handler5(HellwordReq req) {
        HellwordResp resp = new HellwordResp();
        resp.setName(req.getName());
        return CommandResponse.ok(resp);
    }
}

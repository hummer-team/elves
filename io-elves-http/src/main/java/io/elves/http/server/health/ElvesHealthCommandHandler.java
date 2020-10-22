package io.elves.http.server.health;

import io.elves.core.command.CommandMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandle;
import io.elves.core.response.CommandResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ElvesHealthCommandHandler {
    @CommandMapping(name = "/warmup")
    public static class Warmup implements CommandHandle<String> {

        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ofSuccess("ok");
        }
    }

    @CommandMapping(name = "/uptime")
    public static class UpTime implements CommandHandle<String> {
        private static final LocalDateTime date = LocalDateTime.now();

        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ofSuccess(String.format("startAt:%s,upTime:%s"
                    , date.format(DateTimeFormatter.ISO_DATE_TIME)
                    , Duration.between(date, LocalDateTime.now()).toMillis()));
        }
    }
}

package io.elves.http.server.health;

import io.elves.core.command.CommandMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandler;
import io.elves.core.response.CommandResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ElvesHealthCommandHandler {
    @CommandMapping(name = "/warmup")
    public static class Warmup implements CommandHandler<String> {

        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ofSuccess("ok");
        }
    }

    @CommandMapping(name = "/uptime")
    public static class UpTime implements CommandHandler<String> {
        private static final LocalDateTime DATE = LocalDateTime.now();

        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ofSuccess(String.format("startAt:%s,upTime:%s"
                    , DATE.format(DateTimeFormatter.ISO_DATE_TIME)
                    , Duration.between(DATE, LocalDateTime.now()).toMillis()));
        }
    }
}

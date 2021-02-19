package io.elves.http.server.health;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.context.RequestContext;
import io.elves.core.handle.CommandHandler;
import io.elves.core.response.CommandResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ElvesHealthCommandHandler {
    @CommandActionMapping(name = "/warmup")
    public static class Warmup implements CommandHandler<String> {

        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ok("ok");
        }
    }

    @CommandActionMapping(name = "/uptime")
    public static class UpTime implements CommandHandler<String> {
        private static final LocalDateTime DATE = LocalDateTime.now();

        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ok(String.format("startAt:%s,upTime:%s"
                    , DATE.format(DateTimeFormatter.ISO_DATE_TIME)
                    , Duration.between(DATE, LocalDateTime.now()).toMillis()));
        }
    }

    @CommandActionMapping(name = "/")
    public static class Index implements CommandHandler<String> {

        /**
         * implement business process
         *
         * @param context request context
         * @return
         */
        @Override
        public CommandResponse<String> handle(RequestContext context) {
            return CommandResponse.ok("io-elves-http:v1");
        }
    }
}

package io.elves.http.server.health;

import io.elves.core.command.CommandActionMapping;
import io.elves.core.command.CommandHandlerMapping;
import io.elves.core.response.CommandResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * this is system default command handler.
 *
 * @author edz
 */
@CommandHandlerMapping
public class ElvesHealthCommandHandler {
    private static final LocalDateTime DATE = LocalDateTime.now();
    @CommandActionMapping(name = "/warmup")
    public CommandResponse<String> warmup() {
        return CommandResponse.ok("ok");
    }

    @CommandActionMapping(name = "/uptime")
    public CommandResponse<String> uptime() {
        return CommandResponse.ok(String.format("startAt:%s,upTime:%s"
                , DATE.format(DateTimeFormatter.ISO_DATE_TIME)
                , Duration.between(DATE, LocalDateTime.now()).toMillis()));
    }

    @CommandActionMapping(name = "/")
    public CommandResponse<String> index() {
        return CommandResponse.ok("io-elves-http:1.0.0-SNAPSHOT - netty version: 4.1.52.Final");
    }
}

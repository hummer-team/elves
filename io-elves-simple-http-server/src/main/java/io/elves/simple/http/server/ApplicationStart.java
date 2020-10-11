package io.elves.simple.http.server;

import io.elves.core.ElvesBootStart;
import io.elves.http.server.Http2Server;
import io.elves.http.server.HttpServer;

/**
 * @author lee
 */
public class ApplicationStart {
    public static void main(String[] args) {
        ElvesBootStart.run(Http2Server.class, args);
    }
}

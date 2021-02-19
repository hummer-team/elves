package io.elves.simple.http.server;

import io.elves.core.ElvesApplication;
import io.elves.core.ElvesBootStart;
import io.elves.http.server.HttpServer;

/**
 * @author lee
 */
@ElvesApplication(scanPackage = "io.elves", bootServer = HttpServer.class)
public class ApplicationStart {
    public static void main(String[] args) {
        ElvesBootStart.run(ApplicationStart.class, args);
    }
}

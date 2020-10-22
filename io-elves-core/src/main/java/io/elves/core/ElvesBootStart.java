package io.elves.core;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lee
 */
@Slf4j
public class ElvesBootStart {

    private ElvesBootStart() {

    }

    public static void run(Class<?> classz, String[] args) {
        checkIsElvesServer(classz);
        ElvesServer server = null;
        try {
            server = (ElvesServer) classz.newInstance();
            Runtime.getRuntime().addShutdownHook(new Thread(server::close));
            server.init();
            server.start(args);
        } catch (Throwable e) {
            log.error("ElvesServer start failed", e);
            System.exit(1);
        } finally {
            if (server != null) {
                server.close();
            }
        }
    }

    private static void checkIsElvesServer(Class<?> classz) {
        if (!ElvesServer.class.isAssignableFrom(classz)) {
            throw new IllegalArgumentException(String.format("class %s is not elver server."
                    , classz.getName()));
        }
    }
}

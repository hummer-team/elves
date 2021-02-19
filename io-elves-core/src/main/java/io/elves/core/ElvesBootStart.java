package io.elves.core;

/**
 * @author lee
 */
public class ElvesBootStart {

    private ElvesBootStart() {

    }

    public static void run(Class<?> classz, String[] args) {
        ElvesApplication application = classz.getAnnotation(ElvesApplication.class);
        if (application == null) {
            throw new RuntimeException("please appoint ElvesServer.");
        }

        ElvesServer server = null;
        try {
            server = application.bootServer().newInstance();
            Runtime.getRuntime().addShutdownHook(new Thread(server::close));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> ElvesShutdownHook.getAllHoots().forEach(hoot -> {
                try {
                    hoot.run();
                } catch (Throwable e) {
                    System.err.printf("execute shutdown hook fail %s \n", e);
                }
            })));
            server.init(application);
            server.start(args);
        } catch (Throwable e) {
            System.err.printf("ElvesServer start failed %s", e);
            System.exit(0);
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

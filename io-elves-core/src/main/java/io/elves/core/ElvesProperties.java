package io.elves.core;

import static io.elves.core.ElvesConstants.DEFAULT_PORT;

/**
 * property
 *
 * @author lee
 */
public class ElvesProperties {
    private int ioThread;
    private int workThread;
    private String charset;
    private boolean h2;
    private int idleTimeOutSecond;
    private long bufferSize;

    public static int maxRequestContentSize() {
        return 10 * 1024 * 1024;
    }

    public static int getIdleTimeOutSecond(){
        return 45;
    }

    public static boolean enableH2() {
        return false;
    }

    public static int getIoThread() {
        return 1;
    }

    public static int getWorkThread() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    public static int getPort() {
        return DEFAULT_PORT;
    }

    public static int getSslPort() {
        return DEFAULT_PORT + 1;
    }

    public String getCharset() {
        return charset;
    }
}
